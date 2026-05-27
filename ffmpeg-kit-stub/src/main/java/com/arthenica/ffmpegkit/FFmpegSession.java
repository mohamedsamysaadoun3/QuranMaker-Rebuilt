package com.arthenica.ffmpegkit;

public class FFmpegSession {
    private long sessionId = 0;
    public ReturnCode getReturnCode() { return new ReturnCode(0); }
    public String getAllLogsAsString() { return ""; }
    public long getSessionId() { return sessionId; }
}
