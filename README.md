# BentoOS

A minimal Tizen-style OS image for the **Jadoo5** streaming box (Amlogic S905X, ARM64).  
The build produces an **AML Burner-compatible** flash image (`bentoos-jadoo5.img`) that
can be written to the device with Amlogic USB Burning Tool v2/v3.

---

## Repository layout

```
BentoOS/
├── bentoOS/
│   ├── bootanim/           # HTML5 boot animation (boot.html)
│   ├── buttonmapper/       # Button-mapper kernel module sources
│   ├── dts/
│   │   └── amlogic-jadoo5.dts   # Device Tree for Jadoo5 (S905X)
│   ├── scripts/
│   │   ├── setup-aml.sh         # Download toolchain, U-Boot, kernel, AML tools
│   │   ├── build-tizen.sh       # Main build orchestrator (128-core ready)
│   │   └── pack-aml-image.sh    # Pack AML Burner-compatible image
│   ├── fstab.jadoo5        # Partition mount table
│   └── init.jadoo5.rc      # Android-style init script for Jadoo5
├── build/
│   └── config/
│       └── server.txt      # Jadoo5 partition layout reference
└── output/                 # Build artefacts (generated)
    ├── bentoos-jadoo5.img  # Final AML Burner image
    ├── boot.img
    ├── system.img
    ├── data.img
    ├── cache.img
    └── recovery.img
```

---

## Target device

| Property      | Value                                     |
|---------------|-------------------------------------------|
| Device        | Jadoo5 streaming box                      |
| SoC           | Amlogic S905X (GXL family)                |
| Architecture  | ARM64 (AArch64)                           |
| RAM           | 2 GB LPDDR3                               |
| Storage       | eMMC (`/dev/block/platform/d0074000.emmc`)|
| GPU           | Mali-450 MP3                              |
| Flash tool    | AML Burner (USB Burning Tool v2/v3)       |

### Jadoo5 partition layout

| Partition  | Block device  | mmcblk0 | Size    |
|------------|---------------|---------|---------|
| boot       | d0074000.emmc | p9      | 64 MB   |
| system     | d0074000.emmc | p12     | 2 GB    |
| data       | d0074000.emmc | p13     | 4 GB    |
| cache      | d0074000.emmc | p3      | 512 MB  |
| recovery   | d0074000.emmc | p6      | 256 MB  |

---

## Build requirements

### Host OS
Ubuntu 24.04 LTS (also works on 22.04)

### Hardware (recommended for full build)
- **CPU cores:** any modern multi-core CPU (4+ cores); the build auto-detects available cores via `nproc`. Set `JOBS=N` to override. 128-core enterprise servers will complete the build in ~2 hours; a typical 8-core workstation takes ~6-8 hours.
- **RAM:** 16 GB minimum, 64 GB+ recommended for fast parallel builds
- **Storage:** 50 GB free workspace

### Software dependencies
All installed automatically by `setup-aml.sh`:

```
build-essential git wget curl tar bc bison flex
libssl-dev libelf-dev device-tree-compiler u-boot-tools
python3 ccache make
```

---

## Quick-start build

```bash
# 1 — Clone the repo
git clone https://github.com/z556zty6wm-source/BentoOS.git
cd BentoOS

# 2 — Download toolchain, U-Boot, kernel and AML tools  (~30-60 min first run)
bash bentoOS/scripts/setup-aml.sh

# 3 — Build everything  (~2-4 hours with 128 cores, ~1-2 hours rebuild with ccache)
bash bentoOS/scripts/build-tizen.sh

# 4 — Flash the output image with AML Burner
#     Open Amlogic USB Burning Tool → Import Image → select output/bentoos-jadoo5.img
```

### Speed up with ccache

```bash
# Pre-configure ccache before building (100 GB cache = fast rebuilds)
sudo apt-get install -y ccache
mkdir -p ~/.ccache
ccache -M 100G
export CCACHE_DIR=~/.ccache USE_CCACHE=1

# Subsequent builds will take 30-45 minutes instead of 2-4 hours
bash bentoOS/scripts/build-tizen.sh
```

### Override parallel jobs

```bash
JOBS=128 bash bentoOS/scripts/build-tizen.sh
```

---

## Build phases

| # | Phase            | Script               | Time (128 cores) |
|---|------------------|----------------------|------------------|
| 1 | Download sources | `setup-aml.sh`       | 30-60 min        |
| 2 | Build U-Boot     | `build-tizen.sh` §1  | 5-10 min         |
| 3 | Compile DTB      | `build-tizen.sh` §2  | < 1 min          |
| 4 | Build kernel     | `build-tizen.sh` §3  | 15-20 min        |
| 5 | Build rootfs     | `build-tizen.sh` §4  | 2-5 min          |
| 6 | Pack AML image   | `pack-aml-image.sh`  | 5-10 min         |
| **Total** | **First build** | | **~2-4 hours** |
| **Total** | **Rebuild (ccache)** | | **~30-45 min** |

---

## Output

After a successful build `output/bentoos-jadoo5.img` is ready to flash:

```
output/
├── bentoos-jadoo5.img   ← Flash this with AML Burner
├── boot.img
├── system.img
├── data.img
├── cache.img
└── recovery.img
```

The `.img` file is a tar archive containing `image.cfg` (AML Burner XML manifest)
plus the individual partition images.

---

## Flashing

1. Install [Amlogic USB Burning Tool](https://github.com/khadas/utils) on Windows or Linux.
2. Put Jadoo5 into USB Burning mode (hold reset button while plugging USB).
3. Open USB Burning Tool → **Import Image** → select `output/bentoos-jadoo5.img`.
4. Click **Start** and wait ~5-10 minutes.

---

## Components

### Boot animation (`bentoOS/bootanim/boot.html`)
HTML5 terminal-style animation that fades in the BentoOS logo.
Installed to `/system/media/bootanim/boot.html` on the device.

### Button mapper (`bentoOS/buttonmapper/`)
Accessibility-service-based key remapper for the Jadoo5 IR remote.
Sources are installed to `/system/lib/modules/buttonmapper/` for compilation
against the target kernel headers on first boot.

### Device tree (`bentoOS/dts/amlogic-jadoo5.dts`)
Minimal DTS for the Amlogic S905X covering:
CPUs · memory · UART · eMMC · USB · Ethernet · GPIO · IR remote · Mali GPU.
Compiled to `amlogic-jadoo5.dtb` and included in the boot partition.

---

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| `aml-env.sh not found` | Run `setup-aml.sh` first |
| `meson_defconfig not found` | Kernel falls back to `defconfig` automatically |
| Toolchain download fails | Set `TOOLCHAIN_URL` to a mirror in `setup-aml.sh` |
| `mkimage` not found | `sudo apt-get install u-boot-tools` |
| `mkfs.ext4` not found | `sudo apt-get install e2fsprogs` |

---

## License

Open-source components (U-Boot, Linux kernel, device tree) are used under their
respective licences (GPL-2.0).  BentoOS-specific scripts and configuration files
in this repository are provided under the MIT licence.