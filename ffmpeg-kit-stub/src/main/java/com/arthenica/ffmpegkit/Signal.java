package com.arthenica.ffmpegkit;

/* loaded from: classes.dex */
public enum Signal {
    SIGINT(2),
    SIGQUIT(3),
    SIGPIPE(13),
    SIGTERM(15),
    SIGXCPU(24);

    private final int value;

    Signal(int i) {
        this.value = i;
    }

    public int getValue() {
        return this.value;
    }
}
