package com.arthenica.ffmpegkit;

/* loaded from: classes.dex */
public class Statistics {
    private double bitrate;
    private long sessionId;
    private long size;
    private double speed;
    private double time;
    private float videoFps;
    private int videoFrameNumber;
    private float videoQuality;

    public Statistics(long j, int i, float f, float f2, long j2, double d, double d2, double d3) {
        this.sessionId = j;
        this.videoFrameNumber = i;
        this.videoFps = f;
        this.videoQuality = f2;
        this.size = j2;
        this.time = d;
        this.bitrate = d2;
        this.speed = d3;
    }

    public long getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(long j) {
        this.sessionId = j;
    }

    public int getVideoFrameNumber() {
        return this.videoFrameNumber;
    }

    public void setVideoFrameNumber(int i) {
        this.videoFrameNumber = i;
    }

    public float getVideoFps() {
        return this.videoFps;
    }

    public void setVideoFps(float f) {
        this.videoFps = f;
    }

    public float getVideoQuality() {
        return this.videoQuality;
    }

    public void setVideoQuality(float f) {
        this.videoQuality = f;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long j) {
        this.size = j;
    }

    public double getTime() {
        return this.time;
    }

    public void setTime(double d) {
        this.time = d;
    }

    public double getBitrate() {
        return this.bitrate;
    }

    public void setBitrate(double d) {
        this.bitrate = d;
    }

    public double getSpeed() {
        return this.speed;
    }

    public void setSpeed(double d) {
        this.speed = d;
    }

    public String toString() {
        return "Statistics{sessionId=" + this.sessionId + ", videoFrameNumber=" + this.videoFrameNumber + ", videoFps=" + this.videoFps + ", videoQuality=" + this.videoQuality + ", size=" + this.size + ", time=" + this.time + ", bitrate=" + this.bitrate + ", speed=" + this.speed + '}';
    }
}
