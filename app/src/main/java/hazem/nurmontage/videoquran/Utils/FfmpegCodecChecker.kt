package hazem.nurmontage.videoquran.Utils

import android.util.Log

/**
 * FFmpeg codec checker - stub implementation.
 * Full implementation requires FFmpegKit dependency (Phase 7).
 */
object FfmpegCodecChecker {

    data class CodecInfo(
        var videoCodec: String? = null,
        var audioCodec: String? = null,
        var isVideoHwAccelerated: Boolean = false
    )

    interface CodecCallback {
        fun onResult(codecInfo: CodecInfo)
    }

    fun detectCodecsAsync(callback: CodecCallback) {
        // TODO: Implement with FFmpegKit in Phase 7
        // For now, return default codec info
        val info = CodecInfo(
            videoCodec = "libx264",
            audioCodec = "aac",
            isVideoHwAccelerated = false
        )
        callback.onResult(info)
    }
}
