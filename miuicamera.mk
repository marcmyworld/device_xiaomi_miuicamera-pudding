#
# SPDX-FileCopyrightText: The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#

# Verify required source patches are applied during build evaluation
_MIUICAM_PATH := $(patsubst %/,%,$(dir $(lastword $(MAKEFILE_LIST))))
ifneq ($(shell bash $(_MIUICAM_PATH)/apply-source-patches.sh --check --quiet >/dev/null 2>&1 || echo MISSING),)
$(warning [miuicamera-pudding] Required camera source patch(es) in patches-source/ are NOT applied! Run: bash $(_MIUICAM_PATH)/apply-source-patches.sh)
endif

# Inherit proprietary blobs
$(call inherit-product, vendor/xiaomi/miuicamera-pudding/miuicamera-pudding-vendor.mk)

# Soong namespaces
PRODUCT_SOONG_NAMESPACES += \
    device/xiaomi/miuicamera-pudding

# Packages
PRODUCT_PACKAGES += \
    default-permissions-miuicamera.xml \
    hiddenapi-package-allowlist-miuicamera.xml \
    privapp-permissions-miuicamera.xml \
    xiaomi-audiometadata

# Boot jars
PRODUCT_BOOT_JARS += \
    xiaomi-audiometadata

# Properties
PRODUCT_SYSTEM_PROPERTIES += \
    persist.vendor.camera.privapp.list=com.android.camera,org.codeaurora.snapcam \
    vendor.camera.aux.packagelist=com.android.camera,org.codeaurora.snapcam \
    ro.camera.resultFmqSize=4194304

PRODUCT_PRODUCT_PROPERTIES += \
    ro.miui.build.region=eea \
    ro.miui.notch=1 \
    ro.miui.ui.version.code=816 \
    ro.miui.ui.version.name=V816 \
    ro.product.mod_device=pudding_eea_global \
    ro.theme_customize=LCC \
    ro.product.camera.livephoto.support=3

# SELinux
PRODUCT_PRIVATE_SEPOLICY_DIRS += \
    device/xiaomi/miuicamera-pudding/sepolicy/private
