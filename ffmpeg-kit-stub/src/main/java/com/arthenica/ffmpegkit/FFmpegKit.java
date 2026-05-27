package com.arthenica.ffmpegkit;

public class FFmpegKit {
    public static FFmpegSession executeAsync(String command, FFmpegSessionCompleteCallback callback) {
        return new FFmpegSession();
    }
    public static FFmpegSession executeAsync(String[] arguments, FFmpegSessionCompleteCallback callback, LogCallback logCallback, StatisticsCallback statisticsCallback) {
        return new FFmpegSession();
    }
    public static FFmpegSession executeWithArgumentsAsync(String[] arguments, FFmpegSessionCompleteCallback completeCallback) {
        return new FFmpegSession();
    }
    public static FFmpegSession executeWithArgumentsAsync(String[] arguments, FFmpegSessionCompleteCallback completeCallback, LogCallback logCallback, StatisticsCallback statisticsCallback) {
        return new FFmpegSession();
    }
    public static FFmpegSession executeWithArguments(String[] arguments) {
        return new FFmpegSession();
    }
    public static void cancel(long id) {}
    public static void cancel() {}
}
