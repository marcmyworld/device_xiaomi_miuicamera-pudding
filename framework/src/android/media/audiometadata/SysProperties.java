//
// Copyright (C) 2026 The LineageOS Project
//
// SPDX-License-Identifier: Apache-2.0
//

package android.media.audiometadata;

import android.os.SystemProperties;

public class SysProperties {
    public SysProperties() {
    }

    public static String get(String key, String defValue) {
        return SystemProperties.get(key, defValue);
    }

    public static Boolean getBoolean(String key, boolean defValue) {
        return Boolean.valueOf(SystemProperties.getBoolean(key, defValue));
    }

    public static int getInt(String key, int defValue) {
        return SystemProperties.getInt(key, defValue);
    }
}
