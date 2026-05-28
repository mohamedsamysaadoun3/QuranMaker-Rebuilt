package com.arthenica.ffmpegkit;

/* loaded from: classes.dex */
public class MediaInformationSession extends AbstractSession implements Session {
    private final MediaInformationSessionCompleteCallback completeCallback;
    private MediaInformation mediaInformation;

    @Override // com.arthenica.ffmpegkit.Session
    public boolean isFFmpeg() {
        return false;
    }

    @Override // com.arthenica.ffmpegkit.Session
    public boolean isFFprobe() {
        return false;
    }

    @Override // com.arthenica.ffmpegkit.Session
    public boolean isMediaInformation() {
        return true;
    }

    public static MediaInformationSession create(String[] strArr) {
        return new MediaInformationSession(strArr, null, null);
    }

    public static MediaInformationSession create(String[] strArr, MediaInformationSessionCompleteCallback mediaInformationSessionCompleteCallback) {
        return new MediaInformationSession(strArr, mediaInformationSessionCompleteCallback, null);
    }

    public static MediaInformationSession create(String[] strArr, MediaInformationSessionCompleteCallback mediaInformationSessionCompleteCallback, LogCallback logCallback) {
        return new MediaInformationSession(strArr, mediaInformationSessionCompleteCallback, logCallback);
    }

    private MediaInformationSession(String[] strArr, MediaInformationSessionCompleteCallback mediaInformationSessionCompleteCallback, LogCallback logCallback) {
        super(strArr, logCallback, LogRedirectionStrategy.NEVER_PRINT_LOGS);
        this.completeCallback = mediaInformationSessionCompleteCallback;
    }

    public MediaInformation getMediaInformation() {
        return this.mediaInformation;
    }

    public void setMediaInformation(MediaInformation mediaInformation) {
        this.mediaInformation = mediaInformation;
    }

    public MediaInformationSessionCompleteCallback getCompleteCallback() {
        return this.completeCallback;
    }

    public String toString() {
        return "MediaInformationSession{sessionId=" + this.sessionId + ", createTime=" + this.createTime + ", startTime=" + this.startTime + ", endTime=" + this.endTime + ", arguments=" + FFmpegKitConfig.argumentsToString(this.arguments) + ", logs=" + getLogsAsString() + ", state=" + this.state + ", returnCode=" + this.returnCode + ", failStackTrace='" + this.failStackTrace + "'}";
    }
}
