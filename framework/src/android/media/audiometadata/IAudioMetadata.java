//
// Copyright (C) 2026 The LineageOS Project
//
// SPDX-License-Identifier: Apache-2.0
//

package android.media.audiometadata;

import android.content.Context;
import java.nio.ByteBuffer;

public interface IAudioMetadata {
    int getChainLatencyFrames();

    long getExpectedDlb1CorrespondingAudioFramePts(int outputDlb1Frames);

    boolean init(Context context, int sampleRate, int channelCount, int format,
                 boolean audioZoomEnabled, boolean isFrontCamera, int rotation, float ratio);

    int postProcessLatencyFrames();

    ByteBuffer preAnalyze(ByteBuffer audioData, long pts);

    int preAnalyzeLatencyFrames(boolean audioZoomEnabled);

    ByteBuffer preSummarize();

    void release();

    void setCameraFacing(boolean isFront);

    void setDeviceAudioZoomEnabled(boolean enabled);

    void setDeviceRotation(int rotation);

    void setDeviceZoomRatio(float ratio);
}
