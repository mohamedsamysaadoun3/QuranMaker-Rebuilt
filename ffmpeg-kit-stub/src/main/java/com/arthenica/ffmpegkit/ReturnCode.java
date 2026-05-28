package com.arthenica.ffmpegkit;

/* loaded from: classes.dex */
public class ReturnCode {
    public static int CANCEL = 255;
    public static int SUCCESS;
    private final int value;

    public ReturnCode(int i) {
        this.value = i;
    }

    public static boolean isSuccess(ReturnCode returnCode) {
        return returnCode != null && returnCode.getValue() == SUCCESS;
    }

    public static boolean isCancel(ReturnCode returnCode) {
        return returnCode != null && returnCode.getValue() == CANCEL;
    }

    public int getValue() {
        return this.value;
    }

    public boolean isValueSuccess() {
        return this.value == SUCCESS;
    }

    public boolean isValueError() {
        int i = this.value;
        return (i == SUCCESS || i == CANCEL) ? false : true;
    }

    public boolean isValueCancel() {
        return this.value == CANCEL;
    }

    public String toString() {
        return String.valueOf(this.value);
    }
}
