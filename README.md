# Camera device tree for Xiaomi 17 (pudding)

This repository provides the device-specific configurations and patches needed to include miuicamera on aosp roms for `pudding`

## Device specifications

| Camera Sensor | Specification |
| :--- | :--- |
| Main | 50MP (23mm, f/1.67, Light Fusion 950 sensor, OIS) |
| Telephoto | 50MP (60mm floating telephoto, f/2.0, 10cm macro support, OIS) |
| Ultrawide | 50MP (17mm, f/2.4, 102° field of view) |
| Front | 50MP (21mm, f/2.2, autofocus) |

## Usage

To include this camera package in your ROM build, inherit the makefile in your device tree:

```make
$(call inherit-product-if-exists, device/xiaomi/miuicamera-pudding/miuicamera.mk)
```

## Extracting proprietary files

To extract proprietary camera blobs and apply apktool patches from an unpacked firmware image:

```bash
./extract-files.py <path-to-firmware>
```
