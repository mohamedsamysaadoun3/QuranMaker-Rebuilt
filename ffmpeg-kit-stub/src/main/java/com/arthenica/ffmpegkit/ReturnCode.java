package com.arthenica.ffmpegkit;

public class ReturnCode {
    private final int value;
    public ReturnCode(int value) { this.value = value; }
    public int getValue() { return value; }
    public static boolean isSuccess(ReturnCode returnCode) { return returnCode != null && returnCode.getValue() == 0; }
    public static boolean isCancel(ReturnCode returnCode) { return returnCode != null && returnCode.getValue() == 255; }
}
