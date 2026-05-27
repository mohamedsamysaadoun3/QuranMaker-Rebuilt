package hazem.nurmontage.videoquran.model

import java.io.Serializable

class EntityMedia(
    var uri: String? = null,
    var start_original: Int = 0,
    var start: Float = 0f,
    var end: Float = 0f,
    var posX: Float = 0f,
    var posY: Float = 0f,
    var offset_left: Float = 0f,
    var offset_right: Float = 0f,
    var offset: Float = 0f,
    var max: Float = 0f,
    var duration_fade_in: Float = 0f,
    var duration_fade_out: Float = 0f,
    var posXFFmpeg: Float = 0f
) : Serializable {

    var volume: Float = 1.0f
    var isSoundEnable: Boolean = true
    var x: Float = 0f
    var y: Float = 0f
    var w: Float = 1.0f
    var h: Float = 0f
    var mScale: Float = 1.0f
    var topX: Float = 0f
    var topY: Float = 0f
    var time: Int = 0
    var name: String? = null
    var id_raw: Int = 0
    var effectAudio: EffectAudio? = null
    var path_ffmpeg: String? = null
    var path_ffmpeg_effect: String? = null
    var video_path: String? = null
    var paths_https: List<String>? = null
    var isApplyEffectInPreview: Boolean = false
    var index_start_thumbnail: Int = 0
    var index_end_thumbnail: Int = 0

    fun duplicate(): EntityMedia = EntityMedia(
        uri, start_original, start, end,
        posX, posY, offset_left, offset_right,
        offset, max, duration_fade_in, duration_fade_out, posXFFmpeg
    ).also {
        it.x = x
        it.y = y
        it.w = w
        it.h = h
        it.isSoundEnable = isSoundEnable
    }
}
