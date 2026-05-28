package com.arthenica.ffmpegkit;

import java.util.LinkedList;
import java.util.List;

/* loaded from: classes.dex */
public class FFmpegSession extends AbstractSession implements Session {
    private final FFmpegSessionCompleteCallback completeCallback;
    private final List<Statistics> statistics;
    private final StatisticsCallback statisticsCallback;
    private final Object statisticsLock;

    @Override // com.arthenica.ffmpegkit.Session
    public boolean isFFmpeg() {
        return true;
    }

    @Override // com.arthenica.ffmpegkit.Session
    public boolean isFFprobe() {
        return false;
    }

    @Override // com.arthenica.ffmpegkit.Session
    public boolean isMediaInformation() {
        return false;
    }

    public static FFmpegSession create(String[] strArr) {
        return new FFmpegSession(strArr, null, null, null, FFmpegKitConfig.getLogRedirectionStrategy());
    }

    public static FFmpegSession create(String[] strArr, FFmpegSessionCompleteCallback fFmpegSessionCompleteCallback) {
        return new FFmpegSession(strArr, fFmpegSessionCompleteCallback, null, null, FFmpegKitConfig.getLogRedirectionStrategy());
    }

    public static FFmpegSession create(String[] strArr, FFmpegSessionCompleteCallback fFmpegSessionCompleteCallback, LogCallback logCallback, StatisticsCallback statisticsCallback) {
        return new FFmpegSession(strArr, fFmpegSessionCompleteCallback, logCallback, statisticsCallback, FFmpegKitConfig.getLogRedirectionStrategy());
    }

    public static FFmpegSession create(String[] strArr, FFmpegSessionCompleteCallback fFmpegSessionCompleteCallback, LogCallback logCallback, StatisticsCallback statisticsCallback, LogRedirectionStrategy logRedirectionStrategy) {
        return new FFmpegSession(strArr, fFmpegSessionCompleteCallback, logCallback, statisticsCallback, logRedirectionStrategy);
    }

    private FFmpegSession(String[] strArr, FFmpegSessionCompleteCallback fFmpegSessionCompleteCallback, LogCallback logCallback, StatisticsCallback statisticsCallback, LogRedirectionStrategy logRedirectionStrategy) {
        super(strArr, logCallback, logRedirectionStrategy);
        this.completeCallback = fFmpegSessionCompleteCallback;
        this.statisticsCallback = statisticsCallback;
        this.statistics = new LinkedList();
        this.statisticsLock = new Object();
    }

    public StatisticsCallback getStatisticsCallback() {
        return this.statisticsCallback;
    }

    public FFmpegSessionCompleteCallback getCompleteCallback() {
        return this.completeCallback;
    }

    public List<Statistics> getAllStatistics(int i) {
        waitForAsynchronousMessagesInTransmit(i);
        if (thereAreAsynchronousMessagesInTransmit()) {
            android.util.Log.i("ffmpeg-kit", String.format("getAllStatistics was called to return all statistics but there are still statistics being transmitted for session id %d.", Long.valueOf(this.sessionId)));
        }
        return getStatistics();
    }

    public List<Statistics> getAllStatistics() {
        return getAllStatistics(5000);
    }

    public List<Statistics> getStatistics() {
        List<Statistics> list;
        synchronized (this.statisticsLock) {
            list = this.statistics;
        }
        return list;
    }

    public Statistics getLastReceivedStatistics() {
        synchronized (this.statisticsLock) {
            if (this.statistics.size() <= 0) {
                return null;
            }
            return this.statistics.get(this.statistics.size() - 1);
        }
    }

    public void addStatistics(Statistics statistics) {
        synchronized (this.statisticsLock) {
            this.statistics.add(statistics);
        }
    }

    public String toString() {
        return "FFmpegSession{sessionId=" + this.sessionId + ", createTime=" + this.createTime + ", startTime=" + this.startTime + ", endTime=" + this.endTime + ", arguments=" + FFmpegKitConfig.argumentsToString(this.arguments) + ", logs=" + getLogsAsString() + ", state=" + this.state + ", returnCode=" + this.returnCode + ", failStackTrace='" + this.failStackTrace + "'}";
    }
}
