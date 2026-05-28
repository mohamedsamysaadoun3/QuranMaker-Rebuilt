package com.arthenica.ffmpegkit;

/* loaded from: classes.dex */
public class FFprobeSession extends AbstractSession implements Session {
    private final FFprobeSessionCompleteCallback completeCallback;

    @Override // com.arthenica.ffmpegkit.Session
    public boolean isFFmpeg() {
        return false;
    }

    @Override // com.arthenica.ffmpegkit.Session
    public boolean isFFprobe() {
        return true;
    }

    @Override // com.arthenica.ffmpegkit.Session
    public boolean isMediaInformation() {
        return false;
    }

    public static FFprobeSession create(String[] strArr) {
        return new FFprobeSession(strArr, null, null, FFmpegKitConfig.getLogRedirectionStrategy());
    }

    public static FFprobeSession create(String[] strArr, FFprobeSessionCompleteCallback fFprobeSessionCompleteCallback) {
        return new FFprobeSession(strArr, fFprobeSessionCompleteCallback, null, FFmpegKitConfig.getLogRedirectionStrategy());
    }

    public static FFprobeSession create(String[] strArr, FFprobeSessionCompleteCallback fFprobeSessionCompleteCallback, LogCallback logCallback) {
        return new FFprobeSession(strArr, fFprobeSessionCompleteCallback, logCallback, FFmpegKitConfig.getLogRedirectionStrategy());
    }

    public static FFprobeSession create(String[] strArr, FFprobeSessionCompleteCallback fFprobeSessionCompleteCallback, LogCallback logCallback, LogRedirectionStrategy logRedirectionStrategy) {
        return new FFprobeSession(strArr, fFprobeSessionCompleteCallback, logCallback, logRedirectionStrategy);
    }

    private FFprobeSession(String[] strArr, FFprobeSessionCompleteCallback fFprobeSessionCompleteCallback, LogCallback logCallback, LogRedirectionStrategy logRedirectionStrategy) {
        super(strArr, logCallback, logRedirectionStrategy);
        this.completeCallback = fFprobeSessionCompleteCallback;
    }

    public FFprobeSessionCompleteCallback getCompleteCallback() {
        return this.completeCallback;
    }

    public String toString() {
        return "FFprobeSession{sessionId=" + this.sessionId + ", createTime=" + this.createTime + ", startTime=" + this.startTime + ", endTime=" + this.endTime + ", arguments=" + FFmpegKitConfig.argumentsToString(this.arguments) + ", logs=" + getLogsAsString() + ", state=" + this.state + ", returnCode=" + this.returnCode + ", failStackTrace='" + this.failStackTrace + "'}";
    }
}
