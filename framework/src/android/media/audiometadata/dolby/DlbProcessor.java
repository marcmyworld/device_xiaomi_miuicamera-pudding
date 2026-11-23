//
// Copyright (C) 2026 The LineageOS Project
//
// SPDX-License-Identifier: Apache-2.0
//

package android.media.audiometadata.dolby;

import android.util.Log;
import java.nio.ByteBuffer;
import java.util.Locale;

public final class DlbProcessor {
    public static final int ACSE_CHANNEL_ID_2CH_DEFAULT = 0;
    public static final int ACSE_CHANNEL_ID_4CH_VOCAL_OTHERS_DEFAULT = 1;
    public static final int ACSE_CHANNEL_ID_4CH_MUSIC_OTHERS = 2;
    public static final int ACSE_CHANNEL_ID_6CH_VOCAL_MUSIC_OTHERS_DEFAULT = 3;
    public static final int ACSE_CHANNEL_ID_6CH_MUSIC_VOCAL_OTHERS = 4;

    public static final int ACSE_SSEP_OUT_TYPE_NONE = 0;
    public static final int ACSE_SSEP_OUT_TYPE_VOCAL_OTHERS = 1;
    public static final int ACSE_SSEP_OUT_TYPE_MUSIC_OTHERS = 2;

    public static final int DEV_CAPTURE_MODE_EARBUD = 0;
    public static final int DEV_CAPTURE_MODE_PHONE = 1;

    public static final int EDIT_PARAM_AMBIENCE_PRESERVATION = 0x616d7073;
    public static final int EDIT_PARAM_LOUDNESS_CONTROL = 0x6c6f7564;
    public static final int EDIT_PARAM_MUSIC_LOUDNESS_CONTROL = 0x6d75736c;
    public static final int EDIT_PARAM_OTHER_LOUDNESS_CONTROL = 0x6f746c63;
    public static final int EDIT_PARAM_SOUND_SCAPE_WIDTH = 0x736e6477;
    public static final int EDIT_PARAM_VOCAL_LOUDNESS_CONTROL = 0x766f6c63;

    public static final int MODE_CAPTURE_ANALYSIS = 2;
    public static final int MODE_POST_ENHANCEMENT = 3;
    public static final int MODE_POST_SEPARATION = 4;
    public static final int OK = 0;
    public static final String TAG = "DlbProcessor";

    public static boolean dumpAudio = false;
    public static boolean dumpMetadata = false;

    private boolean analyzeRevert = false;
    private int channelID = 0;
    private final int mode;
    private long nativeInstance = 0L;
    private int separateType = 0;

    static {
        dumpMetadata = false;
        dumpAudio = false;
        System.loadLibrary("dolbyacse_jni");
    }

    private DlbProcessor(int mode) {
        this.mode = mode;
    }

    private DlbProcessor(int mode, boolean analyzeRevert) {
        this.mode = mode;
        this.analyzeRevert = analyzeRevert;
    }

    private DlbProcessor(int mode, int separateType) {
        this.mode = mode;
        this.separateType = separateType;
    }

    private DlbProcessor(int mode, int separateType, boolean analyzeRevert) {
        this.mode = mode;
        this.separateType = separateType;
        this.analyzeRevert = analyzeRevert;
    }

    public static DlbProcessor createAnalyzer() {
        return new DlbProcessor(MODE_CAPTURE_ANALYSIS);
    }

    public static DlbProcessor createRevertAnalyzer() {
        return new DlbProcessor(MODE_CAPTURE_ANALYSIS, true);
    }

    public static DlbProcessor createPostProcessor() {
        return new DlbProcessor(MODE_POST_ENHANCEMENT);
    }

    public DlbProcessor createPostSeparator(int type) {
        this.separateType = type;
        return new DlbProcessor(MODE_POST_SEPARATION, this.separateType);
    }

    public DlbProcessor createPostRemaster(int channels) {
        this.channelID = channels;
        return new DlbProcessor(MODE_POST_ENHANCEMENT, this.channelID);
    }

    public static int preAnalyzeLatencyFrames(boolean analyzeRevert) {
        return getFrameLatency(MODE_CAPTURE_ANALYSIS, analyzeRevert);
    }

    public static int postProcessLatencyFrames() {
        return getFrameLatency(MODE_POST_ENHANCEMENT, false);
    }

    public static int postSeparateLatencyFrames() {
        return getFrameLatency(MODE_POST_SEPARATION, false);
    }

    public int getMode() {
        return this.mode;
    }

    public int getSeparateType() {
        return this.separateType;
    }

    public boolean getAnalyzeRevert() {
        return this.analyzeRevert;
    }

    public void init(int sampleRate, int nChannels) {
        if (sampleRate == 48000 && nChannels == 2) {
            Log.d(TAG, "Try to init dlbProcessor with mode " + this.mode);
            long t0 = System.currentTimeMillis();
            this.nativeInstance = nativeInit(this.mode, sampleRate, nChannels, this.separateType, this.channelID,
                    this.analyzeRevert, dumpMetadata, dumpAudio);
            if (this.nativeInstance != 0L) {
                long t1 = System.currentTimeMillis();
                Log.d(TAG, "init dlbProcessor with mode " + this.mode + " return native instance " + this.nativeInstance + " cost " + (t1 - t0) + " ms");
                return;
            }
            throw new IllegalStateException("Failed to initialize DlbProcessor with mode " + this.mode);
        }
        throw new IllegalStateException("Unsupported audio config: rate " + sampleRate + " channel " + nChannels);
    }

    public synchronized void release() {
        if (this.nativeInstance != 0L) {
            Log.d(TAG, "release " + this);
            nativeDestroy();
            this.nativeInstance = 0L;
            return;
        }
        Log.w(TAG, "release object " + this + " while native instance is already released");
    }

    private static String toHexStr(int v) {
        return Integer.toHexString(v).toUpperCase(Locale.ROOT);
    }

    private static String toHexStr(long v) {
        return Long.toHexString(v).toUpperCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return "DlbProcessor object 0x" + toHexStr(hashCode()) + ": mode=" + this.mode +
               ", nativeInstance=0x" + toHexStr(this.nativeInstance);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            synchronized (this) {
                if (this.nativeInstance != 0L) {
                    Log.w(TAG, "finalize object " + this + " without release() called");
                    nativeDestroy();
                    this.nativeInstance = 0L;
                }
            }
        } finally {
            super.finalize();
        }
    }

    // Native methods
    private static native int getFrameLatency(int mode, boolean analyzeRevert);
    private native long nativeInit(int mode, int sampleRate, int nChannels, int separateType, int channelID,
                                   boolean analyzeRevert, boolean dumpMetadata, boolean dumpAudio);
    private native void nativeDestroy();
    public native void reset();
    public static native void setEncodeDelayFrames(int frames);

    public native int preAnalyze(long pts, ByteBuffer audioData, int audioOffset, int audioLimit,
                                 ByteBuffer metaData, int metaLimit);
    public native int estimateSummaryLen();
    public native int preSummarize(ByteBuffer metaData, int metaLimit);
    public native int postEnhanceProcess(long instance, ByteBuffer inBuffer, int inOffset, int inLimit,
                                        ByteBuffer outBuffer, int outOffset, int outLimit);
    public native int separateAudio(long instance, ByteBuffer inBuffer, int inOffset, int inLimit,
                                    ByteBuffer vocalBuffer, int vocalOffset, int vocalLimit,
                                    ByteBuffer otherBuffer, int otherOffset, int otherLimit);
    public native int remasterSeparatedAudio(long instance, ByteBuffer inBuffer, int inOffset, int inLimit,
                                            ByteBuffer outBuffer, int outOffset, int outLimit);
    public native int analyzeRevert(long instance, ByteBuffer inBuffer, int inOffset, int inLimit,
                                    ByteBuffer outBuffer, int outOffset, int outLimit);
    public native int getSeparateMetadata(ByteBuffer inBuffer, int inOffset, int inLimit,
                                          ByteBuffer outBuffer, int outLimit);
    public native int globalMetadataPrepare(ByteBuffer inBuffer, int inOffset, int inLimit, long pts);
    public native void setDeviceCaptureMode(int mode);
    public native void setDeviceAudioZoomEnabled(boolean enabled);
    public native void setSelfieMode(boolean selfie);
    public native void setDeviceRotation(int rotation);
    public native void setDeviceZoomRatio(float ratio);
    public native SegmentInfo[] getDefaultSegments();
    public native void setUserPrefSegments(SegmentInfo[] segments);
}
