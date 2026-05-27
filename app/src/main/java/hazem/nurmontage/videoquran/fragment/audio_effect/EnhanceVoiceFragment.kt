package hazem.nurmontage.videoquran.fragment.audio_effect

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.common.Common
import hazem.nurmontage.videoquran.constant.EffectAudioType
import hazem.nurmontage.videoquran.databinding.FragmentRemoveNoiceBinding
import hazem.nurmontage.videoquran.entity_timeline.EntityAudio
import hazem.nurmontage.videoquran.fragment.EditMediaFragment
import hazem.nurmontage.videoquran.model.EffectAudio
import hazem.nurmontage.videoquran.views.TextCustumFont
import java.util.Locale

/**
 * Fragment for toggling voice enhancement on an audio entity.
 * Reuses the fragment_remove_noice layout but swaps the label text.
 * Builds FFmpeg filter chain identical to RemoveNoiceFragment but with enhance toggle.
 * Converted from EnhanceVoiceFragment.java (201 lines).
 */
class EnhanceVoiceFragment() : Fragment() {

    companion object {
        var instance: EnhanceVoiceFragment? = null

        fun getInstance(
            callback: EditMediaFragment.IEditMediaCallback?,
            entityAudio: EntityAudio?
        ): EnhanceVoiceFragment {
            if (instance == null) {
                instance = EnhanceVoiceFragment(callback, entityAudio)
            }
            return instance!!
        }
    }

    private var binding: FragmentRemoveNoiceBinding? = null
    private var btnPreview: ImageButton? = null
    private var btnRemoveNoice: SwitchCompat? = null
    private var entityAudio: EntityAudio? = null
    private var iEditMediaCallback: EditMediaFragment.IEditMediaCallback? = null
    private var isPlay: Boolean = false


    constructor(
        callback: EditMediaFragment.IEditMediaCallback?,
        entityAudio: EntityAudio?
    ) : this() {
        this.iEditMediaCallback = callback
        this.entityAudio = entityAudio
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflate = FragmentRemoveNoiceBinding.inflate(inflater, container, false)
        binding = inflate
        val root = inflate.root as LinearLayout

        if (iEditMediaCallback != null && entityAudio != null) {
            val switchCompat = root.findViewById<SwitchCompat>(R.id.btn_remove_noice)
            btnRemoveNoice = switchCompat
            switchCompat.isChecked = entityAudio!!.effectAudio.isEnhance

            btnRemoveNoice?.setOnCheckedChangeListener { _: CompoundButton, _: Boolean ->
                apply(false)
            }

            // Override the label to say "Enhance Voice" instead of "Remove Noise"
            (root.findViewById<TextCustumFont>(R.id.tv_remove_noice))
                .text = resources.getString(R.string.enhance_voice)

            root.findViewById<View>(R.id.btn_appl_all).setOnClickListener {
                apply(true)
            }

            root.findViewById<View>(R.id.btn_done).setOnClickListener {
                onDone()
            }

            val imageButton = root.findViewById<ImageButton>(R.id.btn_play)
            btnPreview = imageButton
            imageButton.setOnClickListener {
                preview()
            }
        }

        return root
    }

    fun updateButton() {
        btnPreview?.setImageResource(R.drawable.play_arrow_24px)
        isPlay = false
    }

    private fun preview() {
        val wasPlaying = isPlay
        isPlay = !wasPlaying
        val callback = iEditMediaCallback ?: return
        if (!wasPlaying) {
            callback.startPreview()
            btnPreview?.setImageResource(R.drawable.pause_24px)
        } else {
            callback.pausePreview()
            btnPreview?.setImageResource(R.drawable.play_arrow_24px)
        }
    }

    private fun onDone() {
        iEditMediaCallback?.onDone()
    }

    private fun apply(applyAll: Boolean) {
        val effectAudio = entityAudio!!.effectAudio
        effectAudio.isEnhance = btnRemoveNoice?.isChecked ?: false

        val start = effectAudio.start / 1000.0f
        val end = effectAudio.end / 1000.0f

        val filterList = ArrayList<String>()
        filterList.add(String.format(Locale.US, "atrim=start=%.2f:end=%.2f", start, end))
        filterList.add("asetpts=N/SR/TB")

        if (effectAudio.isRemoveNoice) {
            filterList.add("afftdn=nf=-25")
        }

        filterList.add(String.format(Locale.US, "volume=%.2f", effectAudio.volume))

        if (effectAudio.fade_in > 0) {
            filterList.add("afade=t=in:st=0:d=" + effectAudio.fade_in)
        }
        if (effectAudio.fade_out > 0) {
            val fadeOut = effectAudio.fade_out
            filterList.add("afade=t=out:st=" + ((end - start) - fadeOut) + ":d=" + fadeOut)
        }
        if (effectAudio.isEnhance) {
            filterList.add(Common.ENHANCE_CMD)
        }
        if (effectAudio.reverbPreset != null) {
            filterList.add(effectAudio.reverbPreset!!)
        }
        if (effectAudio.decays > 0) {
            filterList.add(
                String.format(
                    Locale.US, "aecho=%.2f:%.2f:%s:%s",
                    1.0f, effectAudio.outGain,
                    effectAudio.delays_cmd, effectAudio.decays_cmd
                )
            )
        }
        if (effectAudio.speed != 1.0f) {
            filterList.addAll(buildSpeedFilters(effectAudio.speed))
        }

        val callback = iEditMediaCallback ?: return
        if (applyAll) {
            callback.updateEntity(EffectAudioType.ENHANCE, entityAudio!!)
            iEditMediaCallback?.onCmdAll(effectAudio)
        } else {
            iEditMediaCallback?.onCmd(TextUtils.join(",", filterList))
        }
    }

    private fun buildSpeedFilters(speed: Float): List<String> {
        val list = ArrayList<String>()
        var f = speed
        if (f < 0.5f) {
            while (f < 0.5f) {
                list.add("atempo=0.5")
                f /= 0.5f
            }
            list.add(String.format(Locale.US, "atempo=%.2f", f))
        } else if (f > 2.0f) {
            while (f > 2.0f) {
                list.add("atempo=2.0")
                f /= 2.0f
            }
            list.add(String.format(Locale.US, "atempo=%.2f", f))
        } else {
            list.add(String.format(Locale.US, "atempo=%.2f", f))
        }
        return list
    }

    override fun onDestroyView() {
        super.onDestroyView()
        instance = null
        binding = null
    }
}
