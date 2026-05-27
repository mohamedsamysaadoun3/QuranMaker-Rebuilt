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
import hazem.nurmontage.videoquran.databinding.FragmentVolumeBinding
import hazem.nurmontage.videoquran.entity_timeline.EntityAudio
import hazem.nurmontage.videoquran.fragment.EditMediaFragment
import hazem.nurmontage.videoquran.model.EffectAudio
import hazem.nurmontage.videoquran.views.TextCustumFont
import java.util.Locale

/**
 * Fragment for adjusting playback speed of an audio entity.
 * Uses a SeekBar (0-375) mapped to speed range 0.25x–4.0x.
 * Builds FFmpeg filter chain with atempo filters (handles range <0.5 and >2.0 via chaining).
 * Reuses the fragment_volume layout with "Speed" label override.
 * Converted from SpeedFragment.java (226 lines).
 */
class SpeedFragment() : Fragment() {

    companion object {
        var instance: SpeedFragment? = null

        fun getInstance(
            callback: EditMediaFragment.IEditMediaCallback?,
            entityAudio: EntityAudio?
        ): SpeedFragment {
            if (instance == null) {
                instance = SpeedFragment(callback, entityAudio)
            }
            return instance!!
        }
    }

    private var binding: FragmentVolumeBinding? = null
    private var btnPreview: ImageButton? = null
    private var entityAudio: EntityAudio? = null
    private var iVolumeCallback: EditMediaFragment.IEditMediaCallback? = null
    private var isPlay: Boolean = false
    private var tvProgress: TextCustumFont? = null
    private var volumeSeekBar: SeekBar? = null


    constructor(
        callback: EditMediaFragment.IEditMediaCallback?,
        entityAudio: EntityAudio?
    ) : this() {
        this.iVolumeCallback = callback
        this.entityAudio = entityAudio
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflate = FragmentVolumeBinding.inflate(inflater, container, false)
        binding = inflate
        val root = inflate.root as LinearLayout

        val audio = entityAudio
        if (audio?.mediaPlayer != null && iVolumeCallback != null) {
            tvProgress = root.findViewById(R.id.tv_volume_size)

            (root.findViewById<TextCustumFont>(R.id.tv_volume)).text =
                resources.getString(R.string.speed)

            val seekBar = root.findViewById<SeekBar>(R.id.volumeSeekBar)
            volumeSeekBar = seekBar
            seekBar.max = 375

            val speed = ((entityAudio!!.effectAudio.speed - 0.25f) / 3.75f * seekBar.max).toInt()
            seekBar.progress = speed
            tvProgress?.text = String.format(
                Locale.US, "%.2fx",
                speed / 375.0f * 3.75f + 0.25f
            )

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        tvProgress?.text = String.format(
                            Locale.US, "%.2fx",
                            progress / 375.0f * 3.75f + 0.25f
                        )
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    if (isPlay) {
                        previewAudio()
                    }
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    applyVolume(false)
                }
            })

            root.findViewById<View>(R.id.btn_done).setOnClickListener {
                done()
            }

            val imageButton = root.findViewById<ImageButton>(R.id.btn_play)
            btnPreview = imageButton
            imageButton.setOnClickListener {
                previewAudio()
            }

            root.findViewById<View>(R.id.btn_appl_all).setOnClickListener {
                applyVolume(true)
            }
        }

        return root
    }

    private fun done() {
        iVolumeCallback?.pausePreview()
        iVolumeCallback?.onDone()
    }

    private fun previewAudio() {
        val wasPlaying = isPlay
        isPlay = !wasPlaying
        val callback = iVolumeCallback ?: return
        if (!wasPlaying) {
            callback.startPreview()
            btnPreview?.setImageResource(R.drawable.pause_24px)
        } else {
            callback.pausePreview()
            btnPreview?.setImageResource(R.drawable.play_arrow_24px)
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

    private fun applyVolume(applyAll: Boolean) {
        val effectAudio = entityAudio!!.effectAudio
        effectAudio.speed = volumeSeekBar!!.progress / 375.0f * 3.75f + 0.25f

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

        val callback = iVolumeCallback ?: return
        if (applyAll) {
            callback.updateEntity(EffectAudioType.SPEED, entityAudio!!)
            iVolumeCallback?.onCmdAll(effectAudio)
        } else {
            iVolumeCallback?.onCmd(TextUtils.join(",", filterList))
        }
    }

    override fun onDestroyView() {
        iVolumeCallback?.pausePreview()
        super.onDestroyView()
        instance = null
        binding = null
    }

    fun updateButton() {
        btnPreview?.setImageResource(R.drawable.play_arrow_24px)
        isPlay = false
    }
}
