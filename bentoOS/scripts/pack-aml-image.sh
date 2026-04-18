#!/usr/bin/env bash
# pack-aml-image.sh — Produce an AML Burner-compatible flash image
# for BentoOS on Jadoo5 (Amlogic S905X ARM64).
#
# The AML Burner tool (USB Burning Tool v2/v3) expects a specific image
# layout. This script creates that layout from the staged build artefacts.
#
# Partition layout (from build/config/server.txt & bentoOS/fstab.jadoo5):
#   boot      (mmcblk0p9)    — U-Boot + kernel Image + DTB
#   system    (mmcblk0p12)   — rootfs ext4  (2 GB)
#   data      (mmcblk0p13)   — userdata ext4 (4 GB)
#   cache     (mmcblk0p3)    — cache ext4   (512 MB)
#   recovery  (mmcblk0p6)    — recovery ext4 (256 MB)

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
ENV_FILE="${SCRIPT_DIR}/aml-env.sh"

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------
info() { echo -e "\033[1;34m[pack-aml]\033[0m $*"; }
ok()   { echo -e "\033[1;32m[pack-aml]\033[0m $*"; }
warn() { echo -e "\033[1;33m[pack-aml]\033[0m $*"; }
die()  { echo -e "\033[1;31m[pack-aml] ERROR:\033[0m $*" >&2; exit 1; }

require_cmd() { command -v "$1" &>/dev/null || die "Required command '$1' not found."; }

# ---------------------------------------------------------------------------
# Load environment
# ---------------------------------------------------------------------------
[[ -f "${ENV_FILE}" ]] || die "aml-env.sh not found. Run setup-aml.sh first."
# shellcheck source=/dev/null
source "${ENV_FILE}"

OUTPUT_DIR="${OUTPUT_DIR:-${REPO_ROOT}/output}"
STAGING="${BUILD_DIR}/staging"
ROOTFS_DIR="${BUILD_DIR}/rootfs"

# Partition sizes in MiB
BOOT_SIZE_MB=64
SYSTEM_SIZE_MB=2048
DATA_SIZE_MB=4096
CACHE_SIZE_MB=512
RECOVERY_SIZE_MB=256

# ---------------------------------------------------------------------------
# Utility: create a raw ext4 image from a directory
# ---------------------------------------------------------------------------
make_ext4_image() {
    local src_dir="$1"
    local out_img="$2"
    local size_mb="$3"
    local label="$4"

    require_cmd mkfs.ext4

    info "  Creating ${label} ext4 image (${size_mb} MiB)..."
    dd if=/dev/zero of="${out_img}" bs=1M count="${size_mb}" status=none
    mkfs.ext4 -L "${label}" -F "${out_img}" &>/dev/null

    if [[ -d "${src_dir}" && -n "$(ls -A "${src_dir}" 2>/dev/null)" ]]; then
        if command -v debugfs &>/dev/null; then
            # Use debugfs for host-side population (no root needed)
            find "${src_dir}" -mindepth 1 | while read -r f; do
                local rel="${f#${src_dir}/}"
                if [[ -d "$f" ]]; then
                    debugfs -w "${out_img}" -R "mkdir ${rel}" &>/dev/null 2>&1 || true
                elif [[ -f "$f" ]]; then
                    debugfs -w "${out_img}" -R "write ${f} ${rel}" &>/dev/null 2>&1 || true
                fi
            done
        else
            warn "  debugfs not found; ${label} image will be empty (populate on device)."
        fi
    fi
    ok "  ${label}.img created."
}

# ---------------------------------------------------------------------------
# Build boot image (raw: U-Boot BL2 stub + Image + DTB + boot.scr)
# ---------------------------------------------------------------------------
make_boot_image() {
    info "Building boot partition image..."
    local boot_work="${BUILD_DIR}/boot_work"
    local boot_img="${OUTPUT_DIR}/boot.img"
    rm -rf "${boot_work}"
    mkdir -p "${boot_work}"

    # Copy kernel image
    [[ -f "${STAGING}/boot/Image" ]] && cp "${STAGING}/boot/Image" "${boot_work}/"

    # Copy DTBs
    for dtb in "${STAGING}/boot/"*.dtb; do
        [[ -f "${dtb}" ]] && cp "${dtb}" "${boot_work}/"
    done

    # Copy U-Boot binary
    [[ -f "${STAGING}/boot/u-boot.bin" ]] && cp "${STAGING}/boot/u-boot.bin" "${boot_work}/"

    # Generate U-Boot boot script
    local boot_cmd_txt="${boot_work}/boot.cmd"
    cat > "${boot_cmd_txt}" <<'BOOTSCR'
# BentoOS boot script for Jadoo5 (Amlogic S905X)
setenv bootargs "root=/dev/mmcblk0p12 rootfstype=ext4 ro console=ttyAML0,115200n8 no_console_suspend earlycon rw init=/sbin/init"
setenv kernel_addr_r  0x01080000
setenv fdt_addr_r     0x01000000

# Load kernel
ext4load mmc 1:9 ${kernel_addr_r} Image
# Load DTB
ext4load mmc 1:9 ${fdt_addr_r} amlogic-jadoo5.dtb

# Boot
booti ${kernel_addr_r} - ${fdt_addr_r}
BOOTSCR

    if command -v mkimage &>/dev/null; then
        mkimage -C none -A arm64 -T script -d "${boot_cmd_txt}" \
            "${boot_work}/boot.scr" &>/dev/null
        ok "  boot.scr generated."
    else
        warn "  mkimage not found; boot.scr skipped (install u-boot-tools)."
    fi

    # Pack boot partition as ext4
    make_ext4_image "${boot_work}" "${boot_img}" "${BOOT_SIZE_MB}" "boot"
}

# ---------------------------------------------------------------------------
# Build system, data, cache, recovery partition images
# ---------------------------------------------------------------------------
make_partition_images() {
    info "Building system partition image..."
    make_ext4_image "${ROOTFS_DIR}" "${OUTPUT_DIR}/system.img" "${SYSTEM_SIZE_MB}" "system"

    info "Building data partition image (empty, formatted on first boot)..."
    make_ext4_image "" "${OUTPUT_DIR}/data.img" "${DATA_SIZE_MB}" "userdata"

    info "Building cache partition image (empty)..."
    make_ext4_image "" "${OUTPUT_DIR}/cache.img" "${CACHE_SIZE_MB}" "cache"

    info "Building recovery partition image..."
    make_ext4_image "${ROOTFS_DIR}" "${OUTPUT_DIR}/recovery.img" "${RECOVERY_SIZE_MB}" "recovery"
}

# ---------------------------------------------------------------------------
# Write AML Burner image.cfg (XML manifest consumed by USB Burning Tool)
# ---------------------------------------------------------------------------
write_image_cfg() {
    info "Writing AML Burner image.cfg manifest..."
    local cfg="${OUTPUT_DIR}/image.cfg"
    cat > "${cfg}" <<EOF
<?xml version="1.0" encoding="utf-8"?>
<!-- AML Burner image manifest for BentoOS Jadoo5 -->
<!-- Compatible with Amlogic USB Burning Tool v2.x / v3.x             -->
<image>
  <version>v1</version>
  <platform>s905x</platform>
  <partitions>
    <partition name="boot"     file="boot.img"     size="${BOOT_SIZE_MB}M"     type="ext4"/>
    <partition name="system"   file="system.img"   size="${SYSTEM_SIZE_MB}M"   type="ext4"/>
    <partition name="data"     file="data.img"     size="${DATA_SIZE_MB}M"     type="ext4"/>
    <partition name="cache"    file="cache.img"    size="${CACHE_SIZE_MB}M"    type="ext4"/>
    <partition name="recovery" file="recovery.img" size="${RECOVERY_SIZE_MB}M" type="ext4"/>
  </partitions>
</image>
EOF
    ok "  image.cfg written."
}

# ---------------------------------------------------------------------------
# Bundle everything into a single bentoos-jadoo5.img (tar-based AML format)
# ---------------------------------------------------------------------------
bundle_aml_image() {
    info "Bundling final AML Burner image..."
    local final_img="${OUTPUT_DIR}/bentoos-jadoo5.img"
    cd "${OUTPUT_DIR}"

    # Include image.cfg plus all partition images
    tar -czf "${final_img}" \
        image.cfg \
        boot.img \
        system.img \
        data.img \
        cache.img \
        recovery.img

    local size
    size=$(du -sh "${final_img}" | cut -f1)
    ok "Final image: ${final_img} (${size})"
    cd "${REPO_ROOT}"
}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
main() {
    info "=== AML Burner Image Packer ==="
    mkdir -p "${OUTPUT_DIR}"

    require_cmd dd
    require_cmd mkfs.ext4
    require_cmd tar

    make_boot_image
    make_partition_images
    write_image_cfg
    bundle_aml_image

    echo ""
    ok "=== Packing complete ==="
    ok "Flash with AML Burner: ${OUTPUT_DIR}/bentoos-jadoo5.img"
    echo ""
    info "All partition images are also available separately:"
    ls -lh "${OUTPUT_DIR}"/*.img 2>/dev/null || true
}

main "$@"
