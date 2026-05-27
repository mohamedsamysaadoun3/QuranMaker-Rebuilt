package hazem.nurmontage.videoquran.model

import java.io.Serializable

class EffectAudio : Serializable {
    var volume: Float = 1.0f
    var speed: Float = 1.0f
    var isEnhance: Boolean = false
    var isRemoveNoice: Boolean = false
    var start: Float = 0f
    var end: Float = 0f
    var duration: Int = 0
    var reverbPreset_index_list: Int = 0
    var volume_echo: Int = 0
    var decays_cmd: String? = null
    var delays_cmd: String? = null
    var decays: Int = 0
    var delays: Int = 0
    var outGain: Float = 0f
    var reverbPreset: String? = null
    var fade_in: Int = 0
    var fade_out: Int = 0
}
