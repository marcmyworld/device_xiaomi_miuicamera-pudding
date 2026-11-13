# device_xiaomi_miuicamera-pudding

The stock MIUI camera app (`com.android.camera`, 6.5.000340.1) for the Xiaomi 17 (`pudding`),
taken from the stock HyperOS **OS3.0.335.0.XPCEUXM** (EEA) firmware, with what it needs on an
AOSP-based ROM:

- the system-side libraries it loads (its JNI libraries, an OpenCL loader and system copies of
  the DSP RPC libraries for its AI algorithms),
- its privileged and default runtime permissions and hidden API allowlist,
- the camera HAL properties that give it the privileged and auxiliary cameras,
- the stock device, region and HyperOS version properties it identifies the device with.

The camera HAL itself is part of the device tree and its vendor blobs
([device_xiaomi_pudding](https://github.com/xiaomi17-pudding-dev/device_xiaomi_pudding)); this
repo only adds the MIUI camera app on top. It replaces the AOSP and Aperture camera apps.

## Usage

Sync this repo to `device/xiaomi/miuicamera-pudding` and its blobs,
[vendor_xiaomi_miuicamera-pudding](https://gitlab.com/xiaomi17-pudding-dev/vendor_xiaomi_miuicamera-pudding),
to `vendor/xiaomi/miuicamera-pudding`. Both are in the local manifests of
[local_manifests_pudding](https://github.com/xiaomi17-pudding-dev/local_manifests_pudding).
The device tree inherits it with:

```make
$(call inherit-product-if-exists, device/xiaomi/miuicamera-pudding/miuicamera.mk)
```

## Updating the blobs

Extract them from an unpacked stock firmware, as for the device tree:

```bash
cd device/xiaomi/miuicamera-pudding
./extract-files.py <unpacked-firmware>
```

The vendor repo stores `MiuiCamera.apk` (larger than 100 MiB) with Git LFS: install
`git-lfs` and sync with `repo init ... --git-lfs`.
