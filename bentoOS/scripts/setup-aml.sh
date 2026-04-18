#!/usr/bin/env bash
# setup-aml.sh — Download and set up all sources needed to build BentoOS for Jadoo5
# Run once before build-tizen.sh.
# Target: Amlogic S905X / S905X2 ARM64 (Jadoo5 streaming box)
# Host:   Ubuntu 24.04 LTS

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
BUILD_DIR="${REPO_ROOT}/build_workspace"

# ---------------------------------------------------------------------------
# Versions / URLs
# ---------------------------------------------------------------------------
UBOOT_REPO="https://github.com/u-boot/u-boot.git"
UBOOT_TAG="v2024.04"

KERNEL_REPO="https://git.kernel.org/pub/scm/linux/kernel/git/stable/linux.git"
KERNEL_TAG="v6.6.30"

# Linaro AArch64 bare-metal cross-compiler (static binary, no install needed)
TOOLCHAIN_URL="https://releases.linaro.org/components/toolchain/binaries/7.5-2019.12/aarch64-linux-gnu/gcc-linaro-7.5.0-2019.12-x86_64_aarch64-linux-gnu.tar.xz"
TOOLCHAIN_DIR="${BUILD_DIR}/toolchain/gcc-linaro-7.5.0-2019.12-x86_64_aarch64-linux-gnu"

# AML Partition Tool (open-source Amlogic image packer used by AML Burner)
AML_TOOL_REPO="https://github.com/khadas/utils.git"
AML_TOOL_DIR="${BUILD_DIR}/aml-tools"

JOBS="${JOBS:-$(nproc)}"

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------
info()  { echo -e "\033[1;34m[setup-aml]\033[0m $*"; }
ok()    { echo -e "\033[1;32m[setup-aml]\033[0m $*"; }
warn()  { echo -e "\033[1;33m[setup-aml]\033[0m $*"; }
die()   { echo -e "\033[1;31m[setup-aml] ERROR:\033[0m $*" >&2; exit 1; }

require_cmd() {
    command -v "$1" &>/dev/null || die "Required command '$1' not found. Install it first."
}

# ---------------------------------------------------------------------------
# Dependency check
# ---------------------------------------------------------------------------
check_deps() {
    info "Checking host dependencies..."
    local deps=(git wget curl tar xz make gcc bc bison flex libssl-dev libelf-dev
                device-tree-compiler u-boot-tools python3 ccache)
    local missing=()
    for dep in "${deps[@]}"; do
        if ! dpkg -s "$dep" &>/dev/null && ! command -v "$dep" &>/dev/null; then
            missing+=("$dep")
        fi
    done
    if [[ ${#missing[@]} -gt 0 ]]; then
        warn "Installing missing packages: ${missing[*]}"
        sudo apt-get update -y
        sudo apt-get install -y "${missing[@]}"
    fi
    ok "All dependencies satisfied."
}

# ---------------------------------------------------------------------------
# Toolchain
# ---------------------------------------------------------------------------
download_toolchain() {
    if [[ -d "${TOOLCHAIN_DIR}" ]]; then
        ok "Toolchain already present: ${TOOLCHAIN_DIR}"
        return
    fi
    info "Downloading AArch64 cross-compiler..."
    mkdir -p "${BUILD_DIR}/toolchain"
    local archive="${BUILD_DIR}/toolchain/$(basename "${TOOLCHAIN_URL}")"
    wget -c --show-progress -O "${archive}" "${TOOLCHAIN_URL}"
    info "Extracting toolchain..."
    tar -xf "${archive}" -C "${BUILD_DIR}/toolchain"
    ok "Toolchain ready: ${TOOLCHAIN_DIR}"
}

# ---------------------------------------------------------------------------
# U-Boot
# ---------------------------------------------------------------------------
download_uboot() {
    local uboot_dir="${BUILD_DIR}/u-boot"
    if [[ -d "${uboot_dir}/.git" ]]; then
        ok "U-Boot source already present."
        return
    fi
    info "Cloning U-Boot ${UBOOT_TAG}..."
    git clone --depth=1 --branch "${UBOOT_TAG}" "${UBOOT_REPO}" "${uboot_dir}"
    ok "U-Boot cloned: ${uboot_dir}"
}

# ---------------------------------------------------------------------------
# Linux kernel
# ---------------------------------------------------------------------------
download_kernel() {
    local kernel_dir="${BUILD_DIR}/linux"
    if [[ -d "${kernel_dir}/.git" ]]; then
        ok "Linux kernel source already present."
        return
    fi
    info "Cloning Linux kernel ${KERNEL_TAG} (Amlogic ARM64 support included)..."
    git clone --depth=1 --branch "${KERNEL_TAG}" "${KERNEL_REPO}" "${kernel_dir}"
    ok "Kernel cloned: ${kernel_dir}"
}

# ---------------------------------------------------------------------------
# AML partition / image tools
# ---------------------------------------------------------------------------
download_aml_tools() {
    if [[ -d "${AML_TOOL_DIR}/.git" ]]; then
        ok "AML tools already present."
        return
    fi
    info "Cloning Khadas/Amlogic image utilities..."
    git clone --depth=1 "${AML_TOOL_REPO}" "${AML_TOOL_DIR}"
    ok "AML tools cloned: ${AML_TOOL_DIR}"
}

# ---------------------------------------------------------------------------
# Write environment file consumed by build-tizen.sh
# ---------------------------------------------------------------------------
write_env_file() {
    local env_file="${REPO_ROOT}/bentoOS/scripts/aml-env.sh"
    info "Writing environment file: ${env_file}"
    cat > "${env_file}" <<EOF
# Auto-generated by setup-aml.sh — do not edit manually.
export BUILD_DIR="${BUILD_DIR}"
export CROSS_COMPILE="${TOOLCHAIN_DIR}/bin/aarch64-linux-gnu-"
export ARCH="arm64"
export UBOOT_DIR="${BUILD_DIR}/u-boot"
export KERNEL_DIR="${BUILD_DIR}/linux"
export AML_TOOL_DIR="${AML_TOOL_DIR}"
export JOBS="${JOBS}"
export REPO_ROOT="${REPO_ROOT}"
export OUTPUT_DIR="${REPO_ROOT}/output"
EOF
    chmod 644 "${env_file}"
    ok "Environment file written."
}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
main() {
    info "=== BentoOS/Jadoo5 — AML Source Setup ==="
    info "Build workspace : ${BUILD_DIR}"
    info "Parallel jobs   : ${JOBS}"
    echo ""

    mkdir -p "${BUILD_DIR}" "${REPO_ROOT}/output"

    check_deps
    download_toolchain
    download_uboot
    download_kernel
    download_aml_tools
    write_env_file

    echo ""
    ok "=== Setup complete. Run bentoOS/scripts/build-tizen.sh to build. ==="
}

main "$@"
