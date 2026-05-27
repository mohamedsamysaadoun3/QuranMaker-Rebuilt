package hazem.nurmontage.videoquran.Utils

import android.util.Log

/**
 * FFmpeg codec checker - stub implementation.
 * Full implementation requires FFmpegKit dependency (Phase 7).
 */
object FfmpegCodecChecker {

    data class CodecInfo(
        val isHardwareSupported: Boolean = false,
        val preferredCodec: String? = null,
        var videoCodec: String? = null,
        var audioCodec: String? = null,
        var isVideoHwAccelerated: Boolean = false
    )

    fun interface CodecCallback {
        fun onResult(codecInfo: CodecInfo)
    }

    fun detectCodecsAsync(callback: CodecCallback) {
        val info = CodecInfo(
            isHardwareSupported = false,
            preferredCodec = null,
            videoCodec = "libx264",
            audioCodec = "aac",
            isVideoHwAccelerated = false
        )
        callback.onResult(info)
    }
}
