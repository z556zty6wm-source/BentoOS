#!/usr/bin/env bash
# build-tizen.sh — Main BentoOS build orchestrator for Jadoo5 (Amlogic ARM64)
# Builds: U-Boot → Linux kernel → Minimal Tizen rootfs → AML Burner image
#
# Prerequisites: run bentoOS/scripts/setup-aml.sh first.
# Host:          Ubuntu 24.04 LTS
# Parallel jobs: auto-detected (set JOBS=128 to override)

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
ENV_FILE="${SCRIPT_DIR}/aml-env.sh"

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------
info()  { echo -e "\033[1;34m[build-tizen]\033[0m $*"; }
ok()    { echo -e "\033[1;32m[build-tizen]\033[0m $*"; }
die()   { echo -e "\033[1;31m[build-tizen] ERROR:\033[0m $*" >&2; exit 1; }

SECONDS=0
elapsed() { printf '%dh %dm %ds' $((SECONDS/3600)) $(((SECONDS%3600)/60)) $((SECONDS%60)); }

# ---------------------------------------------------------------------------
# Load environment produced by setup-aml.sh
# ---------------------------------------------------------------------------
[[ -f "${ENV_FILE}" ]] || die "Environment file not found. Run setup-aml.sh first."
# shellcheck source=/dev/null
source "${ENV_FILE}"

JOBS="${JOBS:-$(nproc)}"
MAKEFLAGS="-j${JOBS}"
export MAKEFLAGS ARCH CROSS_COMPILE

# Set up ccache if available
if command -v ccache &>/dev/null; then
    export CC="ccache ${CROSS_COMPILE}gcc"
    export CXX="ccache ${CROSS_COMPILE}g++"
    info "ccache enabled (cache dir: ${CCACHE_DIR:-~/.ccache})"
fi

ROOTFS_DIR="${BUILD_DIR}/rootfs"
BOOT_ANIM_SRC="${REPO_ROOT}/bentoOS/bootanim"
BUTTONMAPPER_SRC="${REPO_ROOT}/bentoOS/buttonmapper"
DTS_SRC="${REPO_ROOT}/bentoOS/dts/amlogic-jadoo5.dts"
FSTAB_SRC="${REPO_ROOT}/bentoOS/fstab.jadoo5"
INIT_RC_SRC="${REPO_ROOT}/bentoOS/init.jadoo5.rc"
OUTPUT_DIR="${OUTPUT_DIR:-${REPO_ROOT}/output}"
STAGING="${BUILD_DIR}/staging"

# ---------------------------------------------------------------------------
# Phase 1 — Build U-Boot
# ---------------------------------------------------------------------------
build_uboot() {
    info "[1/5] Building U-Boot for Amlogic S905X (ARM64)..."
    cd "${UBOOT_DIR}"

    # Use generic ARM64 defconfig as baseline; Amlogic-specific configs build
    # on top of it.  khadas-vim defconfig is the closest open-source reference
    # for the S905X family.
    local defconfig="khadas-vim_defconfig"
    if ! make ${MAKEFLAGS} ARCH="${ARCH}" CROSS_COMPILE="${CROSS_COMPILE}" \
            "${defconfig}" 2>/dev/null; then
        warn "khadas-vim_defconfig not available, falling back to generic arm64 defconfig"
        defconfig="qemu_arm64_defconfig"
        make ${MAKEFLAGS} ARCH="${ARCH}" CROSS_COMPILE="${CROSS_COMPILE}" "${defconfig}"
    fi

    make ${MAKEFLAGS} ARCH="${ARCH}" CROSS_COMPILE="${CROSS_COMPILE}" all

    mkdir -p "${STAGING}/boot"
    cp u-boot.bin "${STAGING}/boot/u-boot.bin" 2>/dev/null || \
    cp u-boot     "${STAGING}/boot/u-boot.bin" 2>/dev/null || true

    ok "U-Boot built."
    cd "${REPO_ROOT}"
}

# ---------------------------------------------------------------------------
# Phase 2 — Compile Device Tree Blob
# ---------------------------------------------------------------------------
build_dtb() {
    info "[2/5] Compiling device tree blob..."
    mkdir -p "${STAGING}/boot"
    dtc -I dts -O dtb -o "${STAGING}/boot/amlogic-jadoo5.dtb" "${DTS_SRC}"
    ok "DTB compiled: ${STAGING}/boot/amlogic-jadoo5.dtb"
}

# ---------------------------------------------------------------------------
# Phase 3 — Build Linux Kernel
# ---------------------------------------------------------------------------
build_kernel() {
    info "[3/5] Building Linux kernel (ARM64, Amlogic)..."
    cd "${KERNEL_DIR}"

    # Use defconfig with Amlogic support; fall back to generic arm64 defconfig
    local defconfig="meson_defconfig"
    if ! make ${MAKEFLAGS} ARCH="${ARCH}" CROSS_COMPILE="${CROSS_COMPILE}" \
            "${defconfig}" 2>/dev/null; then
        warn "meson_defconfig not found, using defconfig"
        make ${MAKEFLAGS} ARCH="${ARCH}" CROSS_COMPILE="${CROSS_COMPILE}" defconfig
    fi

    # Enable important options for Jadoo5 via merge fragment
    local _kcfg
    _kcfg="$(mktemp)"
    # shellcheck disable=SC2064
    trap "rm -f '${_kcfg}'" EXIT
    cat > "${_kcfg}" <<'CFG'
CONFIG_AMLOGIC_MESON_GX=y
CONFIG_MESON_GX_MESON8B=y
CONFIG_ARCH_MESON=y
CONFIG_DRM_MESON=y
CONFIG_VIDEO_MESON=y
CONFIG_MEDIA_SUPPORT=y
CONFIG_USB_XHCI_HCD=y
CONFIG_USB_XHCI_MESON_G12A=y
CONFIG_MMC_MESON_GX=y
CONFIG_PINCTRL_MESON_GXL=y
CONFIG_REGULATOR_MESON8B=y
CONFIG_OVERLAY_FS=y
CONFIG_SQUASHFS=y
CONFIG_TMPFS=y
CONFIG_DEVTMPFS=y
CONFIG_DEVTMPFS_MOUNT=y
CONFIG_ZRAM=y
CONFIG_ZSWAP=y
CONFIG_LZ4_COMPRESS=y
CONFIG_CRYPTO_LZ4=y
CONFIG_INIT_ON_ALLOC_DEFAULT_ON=n
CFG
    ./scripts/kconfig/merge_config.sh -m .config "${_kcfg}" 2>/dev/null || true
    rm -f "${_kcfg}"
    trap - EXIT

    make ${MAKEFLAGS} ARCH="${ARCH}" CROSS_COMPILE="${CROSS_COMPILE}" Image modules dtbs

    mkdir -p "${STAGING}/boot" "${STAGING}/lib/modules"
    cp arch/arm64/boot/Image "${STAGING}/boot/Image"

    # External DTB from kernel tree (if built)
    local kdtb="arch/arm64/boot/dts/amlogic/meson-gxl-s905x-khadas-vim.dtb"
    [[ -f "${kdtb}" ]] && cp "${kdtb}" "${STAGING}/boot/meson-gxl-s905x-khadas-vim.dtb"

    make ${MAKEFLAGS} ARCH="${ARCH}" CROSS_COMPILE="${CROSS_COMPILE}" \
        INSTALL_MOD_PATH="${ROOTFS_DIR}" modules_install

    ok "Kernel built."
    cd "${REPO_ROOT}"
}

# ---------------------------------------------------------------------------
# Phase 4 — Build minimal Tizen-style rootfs
# ---------------------------------------------------------------------------
build_rootfs() {
    info "[4/5] Building minimal rootfs..."
    rm -rf "${ROOTFS_DIR}"
    mkdir -p "${ROOTFS_DIR}"/{bin,sbin,lib,lib64,usr/bin,usr/sbin,usr/lib,usr/lib64,\
etc,proc,sys,dev,tmp,run,var/log,var/run,home,root,mnt,media,\
system/bin,system/lib,system/etc,data,cache,recovery,vendor/etc}

    # ---- BentoOS identity ----
    cat > "${ROOTFS_DIR}/etc/os-release" <<EOF
NAME="BentoOS"
VERSION="1.0"
ID=bentoos
ID_LIKE=tizen
VERSION_ID=1.0
PRETTY_NAME="BentoOS 1.0 (Jadoo5)"
BUILD_ID=bentoos-jadoo5-$(date +%Y%m%d)
HOME_URL="https://github.com/z556zty6wm-source/BentoOS"
EOF

    # ---- fstab ----
    cp "${FSTAB_SRC}" "${ROOTFS_DIR}/etc/fstab"

    # ---- init.rc (Amlogic / BentoOS) ----
    cp "${INIT_RC_SRC}" "${ROOTFS_DIR}/etc/init.jadoo5.rc"

    # ---- Boot animation ----
    local bootanim_dest="${ROOTFS_DIR}/system/media/bootanim"
    mkdir -p "${bootanim_dest}"
    cp -r "${BOOT_ANIM_SRC}/." "${bootanim_dest}/"
    ok "  Boot animation installed: ${bootanim_dest}"

    # ---- Button mapper kernel module stub ----
    local btnmap_dest="${ROOTFS_DIR}/system/lib/modules/buttonmapper"
    mkdir -p "${btnmap_dest}"
    # Copy source so it can be compiled in-tree on first boot or by the
    # device vendor toolchain.  A pre-compiled .ko would go here instead.
    cp -r "${BUTTONMAPPER_SRC}/." "${btnmap_dest}/"
    ok "  Button mapper sources installed: ${btnmap_dest}"

    # ---- Minimal /etc skeleton ----
    cat > "${ROOTFS_DIR}/etc/hostname" <<< "jadoo5"

    cat > "${ROOTFS_DIR}/etc/hosts" <<EOF
127.0.0.1  localhost
127.0.1.1  jadoo5
EOF

    cat > "${ROOTFS_DIR}/etc/inittab" <<'EOF'
::sysinit:/etc/init.d/rcS
::respawn:/sbin/getty -L console 115200 vt100
::shutdown:/etc/init.d/rcK
EOF

    mkdir -p "${ROOTFS_DIR}/etc/init.d"
    cat > "${ROOTFS_DIR}/etc/init.d/rcS" <<'EOF'
#!/bin/sh
mount -t proc none /proc
mount -t sysfs none /sys
mount -t devtmpfs none /dev
mount -t tmpfs tmpfs /tmp -o size=64m
echo "BentoOS starting..."
EOF
    chmod +x "${ROOTFS_DIR}/etc/init.d/rcS"

    # ---- BentoOS build metadata ----
    mkdir -p "${ROOTFS_DIR}/system/etc/bentoos"
    cat > "${ROOTFS_DIR}/system/etc/bentoos/build.prop" <<EOF
ro.bentoos.version=1.0
ro.bentoos.device=jadoo5
ro.bentoos.build.date=$(date +%Y%m%d)
ro.bentoos.build.host=$(hostname)
ro.hardware=amlogic
ro.arch=arm64
EOF

    ok "Rootfs skeleton built."
}

# ---------------------------------------------------------------------------
# Phase 5 — Package & produce final image
# ---------------------------------------------------------------------------
package_image() {
    info "[5/5] Packaging AML Burner image..."
    "${SCRIPT_DIR}/pack-aml-image.sh"
    ok "Image packaged."
}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

main() {
    info "=== BentoOS Jadoo5 Build ==="
    info "Parallel jobs : ${JOBS}"
    info "Build dir     : ${BUILD_DIR}"
    info "Output dir    : ${OUTPUT_DIR}"
    echo ""

    mkdir -p "${BUILD_DIR}" "${STAGING}" "${OUTPUT_DIR}"

    build_uboot
    build_dtb
    build_kernel
    build_rootfs
    package_image

    echo ""
    ok "=== Build complete in $(elapsed) ==="
    ok "Output image: ${OUTPUT_DIR}/bentoos-jadoo5.img"
}

main "$@"
