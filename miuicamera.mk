#
# SPDX-FileCopyrightText: The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#

# MIUI camera (com.android.camera) for the Xiaomi 17 (pudding). Inherit it from the device:
#   $(call inherit-product-if-exists, device/xiaomi/miuicamera-pudding/miuicamera.mk)

PRODUCT_SOONG_NAMESPACES += \
    device/xiaomi/miuicamera-pudding

PRODUCT_PACKAGES += \
    default-permissions-miuicamera.xml \
    hiddenapi-package-allowlist-miuicamera.xml \
    privapp-permissions-miuicamera.xml

# The camera HAL gives the MIUI camera the privileged and auxiliary cameras; the HyperOS
# factory test apps of the stock list do not exist here (the value is limited to 91 bytes)
PRODUCT_SYSTEM_PROPERTIES += \
    persist.vendor.camera.privapp.list=com.android.camera,org.codeaurora.snapcam \
    vendor.camera.aux.packagelist=com.android.camera,org.codeaurora.snapcam

# Result metadata queue of 4 MiB (default: the size the camera HAL reports), which the MIUI
# camera's vendor tags need
PRODUCT_SYSTEM_PROPERTIES += \
    ro.camera.resultFmqSize=4194304

# The MIUI camera identifies the device, region and HyperOS version through these stock
# properties (OS3.0.335.0.XPCEUXM values) and picks its per-device camera configuration with
# them; without them portrait and photo capture configure streams the camera HAL rejects
PRODUCT_PRODUCT_PROPERTIES += \
    ro.miui.build.region=eea \
    ro.miui.notch=1 \
    ro.miui.ui.version.code=816 \
    ro.miui.ui.version.name=V816 \
    ro.product.mod_device=pudding_eea_global

# SELinux: the stock labels of these properties
PRODUCT_PRIVATE_SEPOLICY_DIRS += \
    device/xiaomi/miuicamera-pudding/sepolicy/private

$(call inherit-product, vendor/xiaomi/miuicamera-pudding/miuicamera-pudding-vendor.mk)
