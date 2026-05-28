package hazem.nurmontage.videoquran

import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import hazem.nurmontage.videoquran.entity_timeline.EntityAudio
import hazem.nurmontage.videoquran.model.EffectAudio
import java.io.File

class EngineAudioManager(private val activity: EngineActivity) {

    private val id_ffmpeg = mutableListOf<Long>()

    fun createCmd(effect: EffectAudio, startSec: Float, endSec: Float): String {
        val filters = mutableListOf<String>()
        if (startSec > 0 || endSec > 0) {
            filters.add("atrim=start=$startSec:end=$endSec")
            filters.add("asetpts=N/SR/TB")
        }
        if (effect.isRemoveNoice) {
            filters.add("afftdn=nf=-25")
        }
        val volume = effect.volume
        if (volume != 1f) {
            filters.add("volume=${String.format("%.2f", volume)}")
        }
        if (effect.fade_in > 0) {
            filters.add("afade=t=in:st=0:d=${effect.fade_in}")
        }
        if (effect.fade_out > 0) {
            filters.add("afade=t=out:st=${endSec - effect.fade_out}:d=${effect.fade_out}")
        }
        if (effect.isEnhance) {
            filters.add("equalizer=f=300:t=h:w=200:g=3")
            filters.add("equalizer=f=3000:t=h:w=1000:g=4")
            filters.add("equalizer=f=5000:t=h:w=2000:g=2")
        }
        effect.reverbPreset?.let { reverb ->
            if (reverb.isNotEmpty() && reverb != "none") {
                filters.add("aecho=0.8:0.88:60:0.4")
            }
        }
        if (effect.decays != 0 && effect.delays != 0) {
            filters.add("aecho=0.8:0.5:${effect.delays}:${effect.decays}")
        }
        if (effect.speed != 1f && effect.speed > 0) {
            filters.addAll(buildSpeedFilters(effect.speed))
        }
        return filters.joinToString(",")
    }

    fun buildSpeedFilters(speed: Float): List<String> {
        val filters = mutableListOf<String>()
        var remainingSpeed = speed
        while (remainingSpeed > 2.0f) {
            filters.add("atempo=2.0")
            remainingSpeed /= 2.0f
        }
        while (remainingSpeed < 0.5f && remainingSpeed > 0) {
            filters.add("atempo=0.5")
            remainingSpeed /= 0.5f
        }
        if (remainingSpeed in 0.5f..2.0f) {
            filters.add("atempo=${String.format("%.2f", remainingSpeed)}")
        }
        return filters
    }

    fun applyEffect(cmd: String, entityAudio: EntityAudio, onComplete: (() -> Unit)? = null) {
        val inputPath = entityAudio.filePath
        if (inputPath.isEmpty()) return
        val outputFile = File(activity.cacheDir, "effect_${System.currentTimeMillis()}.mp3")
        val ffmpegCmd = "-y -i \"$inputPath\" -af $cmd -y \"${outputFile.absolutePath}\""

        val sessionId = FFmpegKit.executeAsync(ffmpegCmd, { session ->
            if (ReturnCode.isSuccess(session.returnCode)) {
                entityAudio.filePath = outputFile.absolutePath
                activity.runOnUiThread { onComplete?.invoke() }
            } else {
                activity.runOnUiThread { onComplete?.invoke() }
            }
        })
        sessionId?.sessionId?.let { id_ffmpeg.add(it) }
    }

    fun applyEffectAll(effect: EffectAudio, index: Int) {
        val audioList = activity.trackViewEntity.getEntityListAudio()
        if (index >= audioList.size) return
        val entity = audioList[index]
        val startSec = 0f
        val endSec = (entity.endMs - entity.startMs) / 1000f
        val cmd = createCmd(effect, startSec, endSec)
        applyEffect(cmd, entity) {
            if (index + 1 < audioList.size) {
                applyEffectAll(effect, index + 1)
            }
        }
    }

    fun clearFFmpeg() {
        for (id in id_ffmpeg) {
            FFmpegKit.cancel(id)
        }
        id_ffmpeg.clear()
    }
}
