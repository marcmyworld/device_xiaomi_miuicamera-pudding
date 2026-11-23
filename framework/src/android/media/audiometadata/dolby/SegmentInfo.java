//
// Copyright (C) 2026 The LineageOS Project
//
// SPDX-License-Identifier: Apache-2.0
//

package android.media.audiometadata.dolby;

public class SegmentInfo {
    private int ap;
    private int lc;
    private int ml;
    private int ol;
    private long pts;
    private int sw;
    private int vl;

    public SegmentInfo(long pts, int ap, int sw, int lc, int vl, int ml, int ol) {
        this.pts = pts;
        this.ap = ap;
        this.sw = sw;
        this.lc = lc;
        this.vl = vl;
        this.ml = ml;
        this.ol = ol;
    }

    public static SegmentInfo copy(long pts, int ap, int sw, int lc, int vl, int ml, int ol) {
        return new SegmentInfo(pts, ap, sw, lc, vl, ml, ol);
    }

    public static SegmentInfo clone(SegmentInfo info) {
        return copy(info.pts, info.ap, info.sw, info.lc, info.vl, info.ml, info.ol);
    }

    public final long getPts() {
        return this.pts;
    }

    public final void setPts(long pts) {
        this.pts = pts;
    }

    public final int getAp() {
        return this.ap;
    }

    public final void setAp(int ap) {
        this.ap = ap;
    }

    public final int getSw() {
        return this.sw;
    }

    public final void setSw(int sw) {
        this.sw = sw;
    }

    public final int getLc() {
        return this.lc;
    }

    public final void setLc(int lc) {
        this.lc = lc;
    }

    public final int getVl() {
        return this.vl;
    }

    public final void setVl(int vl) {
        this.vl = vl;
    }

    public final int getMl() {
        return this.ml;
    }

    public final void setMl(int ml) {
        this.ml = ml;
    }

    public final int getOl() {
        return this.ol;
    }

    public final void setOl(int ol) {
        this.ol = ol;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SegmentInfo)) {
            return false;
        }
        SegmentInfo o = (SegmentInfo) other;
        return this.pts == o.pts &&
               this.ap == o.ap &&
               this.sw == o.sw &&
               this.lc == o.lc &&
               this.vl == o.vl &&
               this.ml == o.ml &&
               this.ol == o.ol;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(this.pts);
        result = result * 31 + Integer.hashCode(this.ap);
        result = result * 31 + Integer.hashCode(this.sw);
        result = result * 31 + Integer.hashCode(this.lc);
        result = result * 31 + Integer.hashCode(this.vl);
        result = result * 31 + Integer.hashCode(this.ml);
        result = result * 31 + Integer.hashCode(this.ol);
        return result;
    }

    @Override
    public String toString() {
        return "SegmentInfo(pts=" + this.pts +
               ", ap=" + this.ap +
               ", sw=" + this.sw +
               ", lc=" + this.lc +
               ", vl=" + this.vl +
               ", ml=" + this.ml +
               ", ol=" + this.ol + ")";
    }
}
