//
// Copyright (C) 2026 The LineageOS Project
//
// SPDX-License-Identifier: Apache-2.0
//

package android.media.audiometadata;

import android.content.Context;
import android.media.AudioDeviceCallback;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.audiometadata.dolby.DlbProcessor;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.Log;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class DolbyMetadataManager implements IAudioMetadata {
    private static final String TAG = "DolbyMetadataManager";

    private final AudioDeviceCallback mAudioDeviceCallback;
    private AudioManager mAudioManager;
    private DlbProcessor mDlbProcessor;
    private final ArrayList<Integer> mInputDeviceTypes;
    private boolean mIsLoggingEnabled;
    private int nChannelCount = 2;
    private int nFormat = 2;
    private boolean nFrontCamera = false;
    private float nRatio = 1.0f;
    private int nRotation = 0;
    private int nSampleRate = 48000;
    private boolean nZoomEnabled = false;
    private int requestedCompensationFrames = 0;

    public DolbyMetadataManager() {
        this.mInputDeviceTypes = new ArrayList<>(Arrays.asList(
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BLE_HEADSET,
                AudioDeviceInfo.TYPE_USB_HEADSET,
                AudioDeviceInfo.TYPE_WIRED_HEADSET,
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES
        ));
        this.mAudioDeviceCallback = new AudioDeviceCallback() {
            @Override
            public void onAudioDevicesAdded(AudioDeviceInfo[] addedDevices) {
                setIsLocalMicRecording(isLocalMicRecording());
            }

            @Override
            public void onAudioDevicesRemoved(AudioDeviceInfo[] removedDevices) {
                setIsLocalMicRecording(isLocalMicRecording());
            }
        };
        Log.d(TAG, "create dlbProcessor.");
        this.mDlbProcessor = DlbProcessor.createAnalyzer();
    }

    private boolean isCropIn(float num, float min, float max) {
        return num >= min && num <= max;
    }

    private boolean isCropIn(int num, int min, int max) {
        return num >= min && num <= max;
    }

    private boolean isOneOf(int value, int... candidates) {
        for (int c : candidates) {
            if (value == c) {
                return true;
            }
        }
        return false;
    }

    private boolean checkParamters() {
        if (!isCropIn(this.nSampleRate, 48000, 48000)) {
            return false;
        }
        if (!isCropIn(this.nChannelCount, 2, 2)) {
            return false;
        }
        if (!isOneOf(this.nRotation, 0, 90, 180, 270)) {
            return false;
        }
        Log.d(TAG, "checkParamters return true.");
        return true;
    }

    private boolean isLocalMicRecording() {
        if (this.mAudioManager == null) {
            return true;
        }
        AudioDeviceInfo[] devices = this.mAudioManager.getDevices(AudioManager.GET_DEVICES_INPUTS);
        for (AudioDeviceInfo dev : devices) {
            if (this.mInputDeviceTypes.contains(dev.getType())) {
                return false;
            }
        }
        return true;
    }

    private void setIsLocalMicRecording(boolean isLocalMicRecording) {
        Log.d(TAG, "setIsLocalMicRecording isLocalMicRecording: " + isLocalMicRecording);
        if (this.mDlbProcessor != null) {
            this.mDlbProcessor.setDeviceCaptureMode(isLocalMicRecording ? DlbProcessor.DEV_CAPTURE_MODE_PHONE : DlbProcessor.DEV_CAPTURE_MODE_EARBUD);
        }
    }

    @Override
    public boolean init(Context context, int sampleRate, int channelCount, int format,
                        boolean audioZoomEnabled, boolean isFrontCamera, int rotation, float ratio) {
        this.mIsLoggingEnabled = SystemProperties.getBoolean("persist.audio.audiologlevel.debug", false);
        this.nSampleRate = sampleRate;
        this.nChannelCount = channelCount;
        this.nFormat = format;
        this.nZoomEnabled = audioZoomEnabled;
        this.nFrontCamera = isFrontCamera;
        this.nRotation = rotation;
        this.nRatio = ratio;
        Log.d(TAG, "init nSampleRate:" + this.nSampleRate +
                   ", nChannelCount:" + this.nChannelCount +
                   ", nFormat: " + this.nFormat +
                   ", nZoomEnabled:" + this.nZoomEnabled +
                   ", nFrontCamera:" + this.nFrontCamera +
                   ", nRotation:" + this.nRotation +
                   ", nRatio:" + this.nRatio);
        if (!checkParamters()) {
            Log.e(TAG, "init checkParamters is error.");
            return false;
        }
        int chainLatencyFrames = 2;
        this.mDlbProcessor.init(sampleRate, channelCount);
        chainLatencyFrames += DlbProcessor.preAnalyzeLatencyFrames(false) + DlbProcessor.postProcessLatencyFrames();
        this.requestedCompensationFrames = chainLatencyFrames;
        Log.d(TAG, "init preAnalyzeLatencyFrames: " + DlbProcessor.preAnalyzeLatencyFrames(false));
        Log.d(TAG, "init postProcessLatencyFrames: " + DlbProcessor.postProcessLatencyFrames());
        Log.d(TAG, "init requestedCompensationFrames: " + this.requestedCompensationFrames);

        this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.mAudioManager.registerAudioDeviceCallback(this.mAudioDeviceCallback, (Handler) null);
        setDeviceAudioZoomEnabled(this.nZoomEnabled);
        setCameraFacing(this.nFrontCamera);
        setDeviceRotation(this.nRotation);
        setDeviceZoomRatio(this.nRatio);
        return true;
    }

    @Override
    public ByteBuffer preAnalyze(ByteBuffer audioData, long pts) {
        ByteBuffer outputMeta = ByteBuffer.allocateDirect(1024);
        long startTime = System.nanoTime();
        int metaSize = this.mDlbProcessor.preAnalyze(pts, audioData, audioData.position(), audioData.limit(),
                outputMeta, outputMeta.limit());
        if (metaSize < 0 || metaSize > outputMeta.capacity()) {
            Log.e(TAG, "preAnalyze returned invalid metaSize: " + metaSize);
            return null;
        }
        long costTime = (System.nanoTime() - startTime) / 1000L;
        if (costTime > 10000000L) {
            Log.e(TAG, "preAnalyze: dlb1 metaSize=" + metaSize + ", costTime=" + costTime + "us timeout maybe Underrun.");
        }
        if (this.mIsLoggingEnabled) {
            Log.d(TAG, "preAnalyze: dlb1 metaSize=" + metaSize + ", costTime=" + costTime + "us.");
        }
        outputMeta.position(0);
        outputMeta.limit(metaSize);
        return outputMeta;
    }

    @Override
    public ByteBuffer preSummarize() {
        int estimatedSummaryLen = this.mDlbProcessor.estimateSummaryLen();
        Log.d(TAG, "preSummarize: dlb2 estimatedSummaryLen=" + estimatedSummaryLen);
        if (estimatedSummaryLen >= 0 && estimatedSummaryLen <= 409600) {
            ByteBuffer outputMeta = ByteBuffer.allocateDirect(estimatedSummaryLen);
            int metaSize = this.mDlbProcessor.preSummarize(outputMeta, outputMeta.limit());
            Log.d(TAG, "preSummarize: dlb2 metaSize=" + metaSize);
            outputMeta.position(0);
            outputMeta.limit(metaSize);
            return outputMeta;
        }
        Log.e(TAG, "preSummarize: error summary len: " + estimatedSummaryLen);
        return null;
    }

    @Override
    public int preAnalyzeLatencyFrames(boolean analyzeRevert) {
        int frames = DlbProcessor.preAnalyzeLatencyFrames(analyzeRevert);
        Log.d(TAG, "preAnalyzeLatencyFrames: " + frames);
        return frames;
    }

    @Override
    public int postProcessLatencyFrames() {
        return DlbProcessor.postProcessLatencyFrames();
    }

    @Override
    public int getChainLatencyFrames() {
        return this.requestedCompensationFrames;
    }

    @Override
    public long getExpectedDlb1CorrespondingAudioFramePts(int outputDlb1Frames) {
        Log.d(TAG, "getAudioFramePts outputDlb1Frames: " + outputDlb1Frames);
        return (1024000000L / (long) this.nSampleRate) * (long) outputDlb1Frames;
    }

    @Override
    public void setCameraFacing(boolean facingFront) {
        Log.d(TAG, "setCameraFacing facingFront: " + facingFront);
        this.mDlbProcessor.setSelfieMode(facingFront);
    }

    @Override
    public void setDeviceAudioZoomEnabled(boolean enabled) {
        Log.d(TAG, "setDeviceAudioZoomEnabled enabled: " + enabled);
        this.mDlbProcessor.setDeviceAudioZoomEnabled(enabled);
    }

    @Override
    public void setDeviceRotation(int rotation) {
        if (this.mIsLoggingEnabled) {
            Log.d(TAG, "setDeviceRotation rotation: " + rotation);
        }
        this.mDlbProcessor.setDeviceRotation(rotation);
    }

    @Override
    public void setDeviceZoomRatio(float ratio) {
        if (this.mIsLoggingEnabled) {
            Log.d(TAG, "setDeviceZoomRatio ratio: " + ratio);
        }
        this.mDlbProcessor.setDeviceZoomRatio(ratio);
    }

    @Override
    public void release() {
        Log.d(TAG, "release time: " + System.currentTimeMillis());
        if (this.mAudioManager != null) {
            this.mAudioManager.unregisterAudioDeviceCallback(this.mAudioDeviceCallback);
            this.mAudioManager = null;
        }
        if (this.mDlbProcessor != null) {
            this.mDlbProcessor.release();
            this.mDlbProcessor = null;
        }
    }
}
