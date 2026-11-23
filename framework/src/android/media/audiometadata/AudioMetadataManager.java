//
// Copyright (C) 2026 The LineageOS Project
//
// SPDX-License-Identifier: Apache-2.0
//

package android.media.audiometadata;

import android.content.Context;
import android.util.Log;
import java.nio.ByteBuffer;

public class AudioMetadataManager {
    private static final String TAG = "AudioMetadataManager";

    private IAudioMetadata mAudioMetadata;
    private final Object mLock = new Object();
    private boolean mMetadataSupported = false;
    private boolean mReleased = false;

    public AudioMetadataManager() {
        Log.d(TAG, " enter AudioMetadataManager.");
        boolean propEnabled = SysProperties.getBoolean("ro.vendor.audio.metadata.support", false);
        Log.d(TAG, "isDeviceSupportAudioMetadata: " + propEnabled);
        IAudioMetadata manager = null;
        if (propEnabled) {
            int vendorType = SysProperties.getInt("ro.vendor.audio.metadata.type", 0);
            Log.d(TAG, "vendorType: " + vendorType);
            manager = AudioMetadataManagerFactory.getMetadataManager(vendorType);
            if (manager == null) {
                Log.w(TAG, "Failed to getMetadataManager for vendorType =" + vendorType);
            }
        }
        this.mAudioMetadata = manager;
        this.mMetadataSupported = propEnabled && manager != null;
    }

    private void checkReleased() {
        if (this.mReleased) {
            throw new IllegalStateException("AudioMetadataManager has been released and can no longer be used.");
        }
    }

    public boolean isDeviceSupportAudioMetadata() {
        return this.mMetadataSupported;
    }

    public boolean init(Context context, int sampleRate, int channelCount, int format,
                        boolean audioZoomEnabled, boolean isFrontCamera, int rotation, float ratio) {
        synchronized (this.mLock) {
            checkReleased();
            if (this.mAudioMetadata == null) {
                return false;
            }
            return this.mAudioMetadata.init(context, sampleRate, channelCount, format,
                    audioZoomEnabled, isFrontCamera, rotation, ratio);
        }
    }

    public ByteBuffer preAnalyze(ByteBuffer audioData, long pts) {
        synchronized (this.mLock) {
            checkReleased();
            if (this.mAudioMetadata == null) {
                return null;
            }
            return this.mAudioMetadata.preAnalyze(audioData, pts);
        }
    }

    public ByteBuffer preSummarize() {
        synchronized (this.mLock) {
            checkReleased();
            if (this.mAudioMetadata == null) {
                return null;
            }
            return this.mAudioMetadata.preSummarize();
        }
    }

    public int preAnalyzeLatencyFrames(boolean analyzeRevert) {
        synchronized (this.mLock) {
            checkReleased();
            if (this.mAudioMetadata == null) {
                return -1;
            }
            return this.mAudioMetadata.preAnalyzeLatencyFrames(analyzeRevert);
        }
    }

    public int postProcessLatencyFrames() {
        synchronized (this.mLock) {
            checkReleased();
            if (this.mAudioMetadata == null) {
                return -1;
            }
            return this.mAudioMetadata.postProcessLatencyFrames();
        }
    }

    public int getChainLatencyFrames() {
        synchronized (this.mLock) {
            checkReleased();
            if (this.mAudioMetadata == null) {
                return -1;
            }
            return this.mAudioMetadata.getChainLatencyFrames();
        }
    }

    public long getExpectedDlb1CorrespondingAudioFramePts(int outputDlb1Frames) {
        synchronized (this.mLock) {
            checkReleased();
            if (this.mAudioMetadata == null) {
                return -1L;
            }
            return this.mAudioMetadata.getExpectedDlb1CorrespondingAudioFramePts(outputDlb1Frames);
        }
    }

    public void setCameraFacing(boolean facingFront) {
        synchronized (this.mLock) {
            checkReleased();
            if (this.mAudioMetadata != null) {
                this.mAudioMetadata.setCameraFacing(facingFront);
            }
        }
    }

    public void setDeviceAudioZoomEnabled(boolean enabled) {
        synchronized (this.mLock) {
            checkReleased();
            if (this.mAudioMetadata != null) {
                this.mAudioMetadata.setDeviceAudioZoomEnabled(enabled);
            }
        }
    }

    public void setDeviceRotation(int rotation) {
        synchronized (this.mLock) {
            checkReleased();
            if (this.mAudioMetadata != null) {
                this.mAudioMetadata.setDeviceRotation(rotation);
            }
        }
    }

    public void setDeviceZoomRatio(float ratio) {
        synchronized (this.mLock) {
            checkReleased();
            if (this.mAudioMetadata != null) {
                this.mAudioMetadata.setDeviceZoomRatio(ratio);
            }
        }
    }

    public void release() {
        synchronized (this.mLock) {
            if (this.mReleased) {
                return;
            }
            this.mReleased = true;
            if (this.mAudioMetadata != null) {
                this.mAudioMetadata.release();
                this.mAudioMetadata = null;
            }
        }
    }
}
