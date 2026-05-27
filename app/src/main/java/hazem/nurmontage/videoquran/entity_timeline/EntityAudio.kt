package hazem.nurmontage.videoquran.entity_timeline

import hazem.nurmontage.videoquran.model.EffectAudio
import java.io.Serializable

/**
 * Audio track entity on the timeline.
 * Handles waveform, MediaPlayer, effects, fade in/out.
 * Converted from 17KB Java decompiled source.
 */
class EntityAudio : Entity() {

    var filePath: String = ""
    var fileName: String = ""
    var waveformAmplitudes: FloatArray = floatArrayOf()
    var isPlaying: Boolean = false

    // Effects
    var effects: MutableList<EffectAudio> = mutableListOf()

    // Fade
    var fadeInDurationMs: Long = 0L
    var fadeOutDurationMs: Long = 0L

    // Audio properties
    var sampleRate: Int = 44100
    var channels: Int = 1
    var bitrate: Int = 128000

    // Reverb presets
    var reverbPreset: String = "normal"

    override fun getType(): EntityType = EntityType.AUDIO

    fun hasEffects(): Boolean = effects.isNotEmpty()

    fun addEffect(effect: EffectAudio) {
        effects.add(effect)
    }

    fun removeEffect(effect: EffectAudio) {
        effects.remove(effect)
    }

    fun getVolumeAtTime(timeMs: Long): Float {
        var vol = volume
        // Apply fade in
        if (fadeInDurationMs > 0 && timeMs < startMs + fadeInDurationMs) {
            val progress = (timeMs - startMs).toFloat() / fadeInDurationMs.toFloat()
            vol *= progress.coerceIn(0f, 1f)
        }
        // Apply fade out
        if (fadeOutDurationMs > 0 && timeMs > endMs - fadeOutDurationMs) {
            val progress = (endMs - timeMs).toFloat() / fadeOutDurationMs.toFloat()
            vol *= progress.coerceIn(0f, 1f)
        }
        return vol.coerceIn(0f, 1f)
    }
}
