//
// Copyright (C) 2026 The LineageOS Project
//
// SPDX-License-Identifier: Apache-2.0
//

package android.media.audiometadata;

import android.util.Log;

public class AudioMetadataManagerFactory {
    private static final String TAG = "AudioMetadataManagerFactory";

    public AudioMetadataManagerFactory() {
    }

    public static IAudioMetadata getMetadataManager(int vendorType) {
        switch (vendorType) {
            case 0:
                Log.e(TAG, " create AudioMetadataManger error!! Only support Dolby ACSE.");
                return null;
            case 1:
                Log.d(TAG, " create AudioMetadataManger for Dolby ACSE.");
                return new DolbyMetadataManager();
            case 2:
                Log.e(TAG, " create AudioMetadataManger error!! Only support Dolby ACSE.");
                return null;
            default:
                throw new IllegalArgumentException("Unsupported metadata type: " + vendorType);
        }
    }
}
