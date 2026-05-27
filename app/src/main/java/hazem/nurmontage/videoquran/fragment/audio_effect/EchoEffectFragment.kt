package hazem.nurmontage.videoquran.fragment.audio_effect

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.common.Common
import hazem.nurmontage.videoquran.constant.EffectAudioType
import hazem.nurmontage.videoquran.databinding.FragmentEchoEffectBinding
import hazem.nurmontage.videoquran.entity_timeline.EntityAudio
import hazem.nurmontage.videoquran.fragment.EditMediaFragment
import hazem.nurmontage.videoquran.model.EffectAudio
import hazem.nurmontage.videoquran.views.TextCustumFont
import java.util.Locale
import kotlin.math.max
import kotlin.math.pow

/**
 * Echo effect fragment for audio.
 * Controls delay, repeat (decays), and volume for echo effects.
 * Converted from Java decompiled source.
 */
class EchoEffectFragment() : Fragment() {

    companion object {
        var instance: EchoEffectFragment? = null

        fun getInstance(
            iEditMediaCallback: EditMediaFragment.IEditMediaCallback,
            entityAudio: EntityAudio
        ): EchoEffectFragment {
            if (instance == null) {
                instance = EchoEffectFragment(iEditMediaCallback, entityAudio)
            }
            return instance!!
        }
    }

    private var binding: FragmentEchoEffectBinding? = null
    private var btnPreview: ImageButton? = null
    private var delaySeekBar: SeekBar? = null
    private var entityAudio: EntityAudio? = null
    private var iEchoCallback: EditMediaFragment.IEditMediaCallback? = null
    private var isPlay: Boolean = false
    private var repeatSeekBar: SeekBar? = null
    private var tvHintDelay: TextCustumFont? = null
    private var tvHintRepeat: TextCustumFont? = null
    private var tvHintVolume: TextCustumFont? = null
    private var volumeSeekBar: SeekBar? = null


    constructor(
        iEditMediaCallback: EditMediaFragment.IEditMediaCallback,
        entityAudio: EntityAudio
    ) : this() {
        this.iEchoCallback = iEditMediaCallback
        this.entityAudio = entityAudio
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflate = FragmentEchoEffectBinding.inflate(inflater, container, false)
        binding = inflate
        val root: LinearLayout = inflate.root

        if (entityAudio != null && iEchoCallback != null) {
            tvHintDelay = root.findViewById(R.id.tv_delay_size)
            tvHintRepeat = root.findViewById(R.id.tv_repeat_size)
            tvHintVolume = root.findViewById(R.id.tv_volume_size)

            val dSeekBar: SeekBar = root.findViewById(R.id.delaySeekBar)
            delaySeekBar = dSeekBar
            dSeekBar.progress = entityAudio!!.effectAudio.delays

            delaySeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    tvHintDelay?.text = progress.toString()
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    if (isPlay) {
                        iEchoCallback?.pausePreview()
                        updateButton()
                    }
                }
            })

            val rSeekBar: SeekBar = root.findViewById(R.id.repeatSeekBar)
            repeatSeekBar = rSeekBar
            rSeekBar.progress = entityAudio!!.effectAudio.decays

            repeatSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    tvHintRepeat?.text = (progress + 1).toString()
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    if (isPlay) {
                        iEchoCallback?.pausePreview()
                        updateButton()
                    }
                }
            })

            val vSeekBar: SeekBar = root.findViewById(R.id.volumeSeekBar)
            volumeSeekBar = vSeekBar
            vSeekBar.progress = entityAudio!!.effectAudio.volume_echo

            volumeSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    tvHintVolume?.text = progress.toString()
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    if (isPlay) {
                        iEchoCallback?.pausePreview()
                        updateButton()
                    }
                }
            })

            tvHintDelay?.text = delaySeekBar?.progress.toString()
            tvHintRepeat?.text = repeatSeekBar?.progress.toString()
            tvHintVolume?.text = volumeSeekBar?.progress.toString()

            root.findViewById<View>(R.id.btn_done).setOnClickListener { done() }

            val imageButton: ImageButton = root.findViewById(R.id.btn_play)
            btnPreview = imageButton
            imageButton.setOnClickListener { previewAudio() }

            root.findViewById<View>(R.id.btn_appl_all).setOnClickListener { applyEchoEffect(true, false) }
        }

        return root
    }

    fun updateButton() {
        btnPreview?.setImageResource(R.drawable.play_arrow_24px)
        isPlay = false
    }

    private fun applyEchoEffect(applyToAll: Boolean, isPreview: Boolean) {
        var delayProgress = delaySeekBar!!.progress
        val repeatCount = repeatSeekBar!!.progress + 1
        val volumeProgress = volumeSeekBar!!.progress
        val effectAudio = entityAudio!!.effectAudio

        if (!applyToAll &&
            effectAudio.delays == delayProgress &&
            effectAudio.decays == repeatSeekBar!!.progress &&
            effectAudio.volume_echo == volumeProgress
        ) {
            iEchoCallback?.startPreview()
            return
        }

        val start = effectAudio.start / 1000f
        val end = effectAudio.end / 1000f

        effectAudio.decays = repeatSeekBar!!.progress
        effectAudio.delays = delayProgress
        effectAudio.volume_echo = volumeProgress

        val filters = ArrayList<String>()
        filters.add(String.format(Locale.US, "atrim=start=%.2f:end=%.2f", start, end))
        filters.add("asetpts=N/SR/TB")

        if (effectAudio.isRemoveNoice) {
            filters.add("afftdn=nf=-25")
        }

        filters.add(String.format(Locale.US, "volume=%.2f", effectAudio.volume))

        if (effectAudio.fade_in > 0) {
            filters.add("afade=t=in:st=0:d=" + effectAudio.fade_in)
        }
        if (effectAudio.fade_out > 0) {
            val fadeOut = effectAudio.fade_out
            filters.add("afade=t=out:st=" + ((end - start) - fadeOut) + ":d=" + fadeOut)
        }
        if (effectAudio.isEnhance) {
            filters.add(Common.ENHANCE_CMD)
        }
        if (effectAudio.reverbPreset != null) {
            filters.add(effectAudio.reverbPreset!!)
        }

        var speedFactor = 1.0f

        if (effectAudio.decays > 0 && effectAudio.delays > 0) {
            val volumeRatio = volumeProgress / 100.0f
            val decayMultiplier = max(0.01f, 1.0f - volumeRatio)

            val delaysBuilder = StringBuilder()
            val decaysBuilder = StringBuilder()

            for (i in 1..repeatCount) {
                val originalDelay = delayProgress
                val decayValue = max(0.01f, (decayMultiplier * 0.8f.pow(i - 1)).toFloat())

                delaysBuilder.append(delayProgress * i)
                decaysBuilder.append(String.format(Locale.US, "%.2f", decayValue))

                if (i < repeatCount) {
                    delaysBuilder.append("|")
                    decaysBuilder.append("|")
                }

                delayProgress = originalDelay
            }

            val outGain = max(0.01f, volumeRatio)
            effectAudio.outGain = outGain
            effectAudio.decays_cmd = decaysBuilder.toString()
            effectAudio.delays_cmd = delaysBuilder.toString()

            speedFactor = 1.0f
            filters.add(
                String.format(
                    Locale.US,
                    "aecho=%.2f:%.2f:%s:%s",
                    1.0f,
                    outGain,
                    delaysBuilder,
                    decaysBuilder
                )
            )
        }

        if (effectAudio.speed != speedFactor) {
            filters.addAll(buildSpeedFilters(effectAudio.speed))
        }

        iEchoCallback?.let { callback ->
            if (applyToAll) {
                callback.updateEntity(EffectAudioType.ECHO, entityAudio!!)
                callback.onCmdAll(effectAudio)
            } else {
                val joined = TextUtils.join(",", filters)
                if (isPreview) {
                    callback.onCmdPlay(joined)
                } else {
                    callback.onCmd(joined)
                }
            }
        }
    }

    private fun buildSpeedFilters(speed: Float): List<String> {
        val filters = ArrayList<String>()
        var s = speed
        if (s < 0.5f) {
            while (s < 0.5f) {
                filters.add("atempo=0.5")
                s /= 0.5f
            }
            filters.add(String.format(Locale.US, "atempo=%.2f", s))
        } else if (s > 2.0f) {
            while (s > 2.0f) {
                filters.add("atempo=2.0")
                s /= 2.0f
            }
            filters.add(String.format(Locale.US, "atempo=%.2f", s))
        } else {
            filters.add(String.format(Locale.US, "atempo=%.2f", s))
        }
        return filters
    }

    private fun done() {
        iEchoCallback?.let { callback ->
            val effectAudio = entityAudio!!.effectAudio
            if (effectAudio.delays != delaySeekBar?.progress ||
                effectAudio.decays != repeatSeekBar?.progress ||
                effectAudio.volume_echo != volumeSeekBar?.progress
            ) {
                applyEchoEffect(false, false)
            }
            callback.pausePreview()
            callback.onDone()
        }
    }

    private fun previewAudio() {
        val wasPlaying = isPlay
        isPlay = !wasPlaying
        iEchoCallback?.let { callback ->
            if (!wasPlaying) {
                applyEchoEffect(false, true)
                btnPreview?.setImageResource(R.drawable.pause_24px)
            } else {
                callback.pausePreview()
                btnPreview?.setImageResource(R.drawable.play_arrow_24px)
            }
        }
    }

    override fun onDestroyView() {
        iEchoCallback?.pausePreview()
        super.onDestroyView()
        instance = null
        binding = null
    }
}
