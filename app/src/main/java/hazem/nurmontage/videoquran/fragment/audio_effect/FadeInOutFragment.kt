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
import hazem.nurmontage.videoquran.databinding.FragmentFadeInOutBinding
import hazem.nurmontage.videoquran.entity_timeline.EntityAudio
import hazem.nurmontage.videoquran.fragment.EditMediaFragment
import hazem.nurmontage.videoquran.model.EffectAudio
import hazem.nurmontage.videoquran.views.TextCustumFont
import java.util.Locale

/**
 * Fade In/Out audio effect fragment.
 * Allows setting fade-in and fade-out durations for audio clips.
 * Converted from Java decompiled source.
 */
class FadeInOutFragment() : Fragment() {

    companion object {
        var instance: FadeInOutFragment? = null

        fun getInstance(
            iEditMediaCallback: EditMediaFragment.IEditMediaCallback,
            entityAudio: EntityAudio
        ): FadeInOutFragment {
            if (instance == null) {
                instance = FadeInOutFragment(iEditMediaCallback, entityAudio)
            }
            return instance!!
        }
    }

    private var binding: FragmentFadeInOutBinding? = null
    private var btnPreview: ImageButton? = null
    private var entityAudio: EntityAudio? = null
    private var fadeInSeekBar: SeekBar? = null
    private var fadeOutSeekBar: SeekBar? = null
    private var hintFadeIn: TextCustumFont? = null
    private var hintFadeOut: TextCustumFont? = null
    private var iEditMediaCallback: EditMediaFragment.IEditMediaCallback? = null
    private var isPlay: Boolean = false


    constructor(
        iEditMediaCallback: EditMediaFragment.IEditMediaCallback,
        entityAudio: EntityAudio
    ) : this() {
        this.iEditMediaCallback = iEditMediaCallback
        this.entityAudio = entityAudio
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflate = FragmentFadeInOutBinding.inflate(inflater, container, false)
        binding = inflate
        val root: LinearLayout = inflate.root

        val audio = entityAudio
        if (audio == null || audio.mediaPlayer == null) {
            return root
        }

        hintFadeIn = root.findViewById(R.id.hint_fade_in)
        hintFadeOut = root.findViewById(R.id.hint_fade_out)
        fadeInSeekBar = root.findViewById(R.id.fadeInSeekBar)
        fadeOutSeekBar = root.findViewById(R.id.fadeOutSeekBar)

        val secondInScreen = audio.secondInScreen
        val rect = audio.getRect()
        val maxFadeDuration = ((rect.right / secondInScreen) - (rect.left / secondInScreen)) * 0.5f
        val maxFadeInt = maxFadeDuration.toInt()

        fadeInSeekBar?.max = maxFadeInt
        fadeOutSeekBar?.max = maxFadeInt
        fadeInSeekBar?.progress = audio.effectAudio.fade_in
        fadeOutSeekBar?.progress = audio.effectAudio.fade_out
        hintFadeIn?.text = fadeInSeekBar?.progress.toString()
        hintFadeOut?.text = fadeOutSeekBar?.progress.toString()

        fadeInSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                hintFadeIn?.text = progress.toString()
            }
        })

        fadeOutSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                hintFadeOut?.text = progress.toString()
            }
        })

        root.findViewById<View>(R.id.btn_done).setOnClickListener { done() }

        val imageButton: ImageButton = root.findViewById(R.id.btn_play)
        btnPreview = imageButton
        imageButton.setOnClickListener { previewAudio() }

        root.findViewById<View>(R.id.btn_appl_all).setOnClickListener { applyFade(true, false) }

        return root
    }

    private fun done() {
        iEditMediaCallback?.let { callback ->
            val effectAudio = entityAudio!!.effectAudio
            if (effectAudio.fade_in != fadeInSeekBar?.progress || effectAudio.fade_out != fadeOutSeekBar?.progress) {
                applyFade(false, false)
            }
            callback.pausePreview()
            callback.onDone()
        }
    }

    private fun previewAudio() {
        val wasPlaying = isPlay
        isPlay = !wasPlaying
        iEditMediaCallback?.let { callback ->
            if (!wasPlaying) {
                applyFade(false, true)
                btnPreview?.setImageResource(R.drawable.pause_24px)
            } else {
                callback.pausePreview()
                btnPreview?.setImageResource(R.drawable.play_arrow_24px)
            }
        }
    }

    private fun applyFade(applyToAll: Boolean, isPreview: Boolean) {
        val effectAudio = entityAudio!!.effectAudio
        val audio = entityAudio!!

        if (audio.fadeInDurationMs.toInt() == fadeInSeekBar?.progress &&
            audio.fadeOutDurationMs.toInt() == fadeOutSeekBar?.progress
        ) {
            if (applyToAll) {
                iEditMediaCallback?.onDone()
                return
            }
            return
        }

        effectAudio.fade_in = fadeInSeekBar?.progress ?: 0
        effectAudio.fade_out = fadeOutSeekBar?.progress ?: 0

        val start = effectAudio.start / 1000f
        val end = effectAudio.end / 1000f

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
        if (effectAudio.decays > 0) {
            filters.add(
                String.format(
                    Locale.US,
                    "aecho=%.2f:%.2f:%s:%s",
                    1.0f,
                    effectAudio.outGain,
                    effectAudio.delays_cmd,
                    effectAudio.decays_cmd
                )
            )
        }
        if (effectAudio.speed != 1.0f) {
            filters.addAll(buildSpeedFilters(effectAudio.speed))
        }

        val joined = TextUtils.join(",", filters)
        iEditMediaCallback?.let { callback ->
            if (applyToAll) {
                callback.updateEntity(EffectAudioType.FADE, entityAudio!!)
                callback.onCmdAll(effectAudio)
            } else if (isPreview) {
                callback.onCmdPlay(joined)
            } else {
                callback.onCmd(joined)
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

    fun updateButton() {
        btnPreview?.setImageResource(R.drawable.play_arrow_24px)
        isPlay = false
    }

    override fun onDestroyView() {
        iEditMediaCallback?.pausePreview()
        super.onDestroyView()
        instance = null
        binding = null
    }
}
