package com.arthenica.ffmpegkit;

import java.util.List;
import java.util.concurrent.ExecutorService;

/* loaded from: classes.dex */
public class FFprobeKit {
    private FFprobeKit() {
    }

    private static String[] defaultGetMediaInformationCommandArguments(String str) {
        return new String[]{"-v", "error", "-hide_banner", "-print_format", "json", "-show_format", "-show_streams", "-show_chapters", "-i", str};
    }

    public static FFprobeSession executeWithArguments(String[] strArr) {
        FFprobeSession create = FFprobeSession.create(strArr);
        FFmpegKitConfig.ffprobeExecute(create);
        return create;
    }

    public static FFprobeSession executeWithArgumentsAsync(String[] strArr, FFprobeSessionCompleteCallback fFprobeSessionCompleteCallback) {
        FFprobeSession create = FFprobeSession.create(strArr, fFprobeSessionCompleteCallback);
        FFmpegKitConfig.asyncFFprobeExecute(create);
        return create;
    }

    public static FFprobeSession executeWithArgumentsAsync(String[] strArr, FFprobeSessionCompleteCallback fFprobeSessionCompleteCallback, LogCallback logCallback) {
        FFprobeSession create = FFprobeSession.create(strArr, fFprobeSessionCompleteCallback, logCallback);
        FFmpegKitConfig.asyncFFprobeExecute(create);
        return create;
    }

    public static FFprobeSession executeWithArgumentsAsync(String[] strArr, FFprobeSessionCompleteCallback fFprobeSessionCompleteCallback, ExecutorService executorService) {
        FFprobeSession create = FFprobeSession.create(strArr, fFprobeSessionCompleteCallback);
        FFmpegKitConfig.asyncFFprobeExecute(create, executorService);
        return create;
    }

    public static FFprobeSession executeWithArgumentsAsync(String[] strArr, FFprobeSessionCompleteCallback fFprobeSessionCompleteCallback, LogCallback logCallback, ExecutorService executorService) {
        FFprobeSession create = FFprobeSession.create(strArr, fFprobeSessionCompleteCallback, logCallback);
        FFmpegKitConfig.asyncFFprobeExecute(create, executorService);
        return create;
    }

    public static FFprobeSession execute(String str) {
        return executeWithArguments(FFmpegKitConfig.parseArguments(str));
    }

    public static FFprobeSession executeAsync(String str, FFprobeSessionCompleteCallback fFprobeSessionCompleteCallback) {
        return executeWithArgumentsAsync(FFmpegKitConfig.parseArguments(str), fFprobeSessionCompleteCallback);
    }

    public static FFprobeSession executeAsync(String str, FFprobeSessionCompleteCallback fFprobeSessionCompleteCallback, LogCallback logCallback) {
        return executeWithArgumentsAsync(FFmpegKitConfig.parseArguments(str), fFprobeSessionCompleteCallback, logCallback);
    }

    public static FFprobeSession executeAsync(String str, FFprobeSessionCompleteCallback fFprobeSessionCompleteCallback, ExecutorService executorService) {
        FFprobeSession create = FFprobeSession.create(FFmpegKitConfig.parseArguments(str), fFprobeSessionCompleteCallback);
        FFmpegKitConfig.asyncFFprobeExecute(create, executorService);
        return create;
    }

    public static FFprobeSession executeAsync(String str, FFprobeSessionCompleteCallback fFprobeSessionCompleteCallback, LogCallback logCallback, ExecutorService executorService) {
        FFprobeSession create = FFprobeSession.create(FFmpegKitConfig.parseArguments(str), fFprobeSessionCompleteCallback, logCallback);
        FFmpegKitConfig.asyncFFprobeExecute(create, executorService);
        return create;
    }

    public static MediaInformationSession getMediaInformation(String str) {
        MediaInformationSession create = MediaInformationSession.create(defaultGetMediaInformationCommandArguments(str));
        FFmpegKitConfig.getMediaInformationExecute(create, 5000);
        return create;
    }

    public static MediaInformationSession getMediaInformation(String str, int i) {
        MediaInformationSession create = MediaInformationSession.create(defaultGetMediaInformationCommandArguments(str));
        FFmpegKitConfig.getMediaInformationExecute(create, i);
        return create;
    }

    public static MediaInformationSession getMediaInformationAsync(String str, MediaInformationSessionCompleteCallback mediaInformationSessionCompleteCallback) {
        MediaInformationSession create = MediaInformationSession.create(defaultGetMediaInformationCommandArguments(str), mediaInformationSessionCompleteCallback);
        FFmpegKitConfig.asyncGetMediaInformationExecute(create, 5000);
        return create;
    }

    public static MediaInformationSession getMediaInformationAsync(String str, MediaInformationSessionCompleteCallback mediaInformationSessionCompleteCallback, LogCallback logCallback, int i) {
        MediaInformationSession create = MediaInformationSession.create(defaultGetMediaInformationCommandArguments(str), mediaInformationSessionCompleteCallback, logCallback);
        FFmpegKitConfig.asyncGetMediaInformationExecute(create, i);
        return create;
    }

    public static MediaInformationSession getMediaInformationAsync(String str, MediaInformationSessionCompleteCallback mediaInformationSessionCompleteCallback, ExecutorService executorService) {
        MediaInformationSession create = MediaInformationSession.create(defaultGetMediaInformationCommandArguments(str), mediaInformationSessionCompleteCallback);
        FFmpegKitConfig.asyncGetMediaInformationExecute(create, executorService, 5000);
        return create;
    }

    public static MediaInformationSession getMediaInformationAsync(String str, MediaInformationSessionCompleteCallback mediaInformationSessionCompleteCallback, LogCallback logCallback, ExecutorService executorService, int i) {
        MediaInformationSession create = MediaInformationSession.create(defaultGetMediaInformationCommandArguments(str), mediaInformationSessionCompleteCallback, logCallback);
        FFmpegKitConfig.asyncGetMediaInformationExecute(create, executorService, i);
        return create;
    }

    public static MediaInformationSession getMediaInformationFromCommand(String str) {
        MediaInformationSession create = MediaInformationSession.create(FFmpegKitConfig.parseArguments(str));
        FFmpegKitConfig.getMediaInformationExecute(create, 5000);
        return create;
    }

    public static MediaInformationSession getMediaInformationFromCommandAsync(String str, MediaInformationSessionCompleteCallback mediaInformationSessionCompleteCallback, LogCallback logCallback, int i) {
        return getMediaInformationFromCommandArgumentsAsync(FFmpegKitConfig.parseArguments(str), mediaInformationSessionCompleteCallback, logCallback, i);
    }

    private static MediaInformationSession getMediaInformationFromCommandArgumentsAsync(String[] strArr, MediaInformationSessionCompleteCallback mediaInformationSessionCompleteCallback, LogCallback logCallback, int i) {
        MediaInformationSession create = MediaInformationSession.create(strArr, mediaInformationSessionCompleteCallback, logCallback);
        FFmpegKitConfig.asyncGetMediaInformationExecute(create, i);
        return create;
    }

    public static List<FFprobeSession> listFFprobeSessions() {
        return FFmpegKitConfig.getFFprobeSessions();
    }

    public static List<MediaInformationSession> listMediaInformationSessions() {
        return FFmpegKitConfig.getMediaInformationSessions();
    }
}
