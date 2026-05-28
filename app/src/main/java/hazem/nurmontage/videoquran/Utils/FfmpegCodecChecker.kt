package hazem.nurmontage.videoquran.Utils

import android.media.MediaCodecList
import android.os.Build
import android.util.Log

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

    private const val TAG = "FfmpegCodecChecker"

    fun detectCodecsAsync(callback: CodecCallback) {
        Thread {
            val info = detectCodecs()
            callback.onResult(info)
        }.start()
    }

    fun detectCodecs(): CodecInfo {
        var isHwSupported = false
        var preferredCodec: String? = null

        try {
            val codecList = MediaCodecList(android.media.MediaCodecList.REGULAR_CODECS)
            for (info in codecList.codecInfos) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && info.isAlias) continue
                val name = info.name.lowercase()
                if (info.isEncoder && name.contains("avc")) {
                    if (name.contains("omx") || name.contains("c2") || name.contains("android")) {
                        isHwSupported = true
                        preferredCodec = info.name
                        break
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "MediaCodecList query failed", e)
        }

        return CodecInfo(
            isHardwareSupported = isHwSupported,
            preferredCodec = preferredCodec,
            videoCodec = if (isHwSupported) preferredCodec else "libx264",
            audioCodec = "aac",
            isVideoHwAccelerated = isHwSupported
        )
    }
}
