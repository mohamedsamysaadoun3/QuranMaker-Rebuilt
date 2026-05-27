package hazem.nurmontage.videoquran.fragment.audio_effect

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.adabter.ReverbeAdabter
import hazem.nurmontage.videoquran.common.Common
import hazem.nurmontage.videoquran.constant.EffectAudioType
import hazem.nurmontage.videoquran.databinding.FragmentReverbePresetBinding
import hazem.nurmontage.videoquran.entity_timeline.EntityAudio
import hazem.nurmontage.videoquran.fragment.EditMediaFragment
import hazem.nurmontage.videoquran.model.EffectAudio
import java.util.Locale

/**
 * Fragment for selecting a reverb preset for an audio entity.
 * Provides a list of reverb presets (Normal, Masjid, Studio, etc.) each with an FFmpeg command.
 * When a preset is selected, builds and applies the complete filter chain with live preview.
 * Converted from ReverbePresetFragment.java (211 lines).
 */
class ReverbePresetFragment() : Fragment() {

    companion object {
        var instance: ReverbePresetFragment? = null

        fun getInstance(
            callback: EditMediaFragment.IEditMediaCallback?,
            entityAudio: EntityAudio?
        ): ReverbePresetFragment {
            if (instance == null) {
                instance = ReverbePresetFragment(callback, entityAudio)
            }
            return instance!!
        }
    }

    private var binding: FragmentReverbePresetBinding? = null
    private var entityAudio: EntityAudio? = null
    private var iEditMediaCallback: EditMediaFragment.IEditMediaCallback? = null

    private var iReverbPresetCallback: ReverbeAdabter.IReverbPresetCallback? =
        object : ReverbeAdabter.IReverbPresetCallback {
            override fun cmd(cmd: String?, index: Int) {
                if (iEditMediaCallback == null) return
                val effectAudio = entityAudio!!.effectAudio

                if (cmd == null && effectAudio.reverbPreset == null) {
                    iEditMediaCallback!!.startPreview()
                    return
                }

                effectAudio.reverbPreset = cmd
                effectAudio.reverbPreset_index_list = index

                val start = effectAudio.start / 1000.0f
                val end = effectAudio.end / 1000.0f
                val duration = end - start

                val filterList = ArrayList<String>()
                filterList.add(String.format(Locale.US, "atrim=start=%.2f:end=%.2f", start, end))
                filterList.add("asetpts=N/SR/TB")

                if (effectAudio.isRemoveNoice) {
                    filterList.add("afftdn=nf=-25")
                }

                filterList.add(String.format(Locale.US, "volume=%.2f", effectAudio.volume))

                if (effectAudio.fade_in > 0) {
                    filterList.add(
                        String.format(
                            Locale.US, "afade=t=in:st=0:d=%.2f",
                            effectAudio.fade_in / 1000.0f
                        )
                    )
                }
                if (effectAudio.fade_out > 0) {
                    val fadeOut = effectAudio.fade_out / 1000.0f
                    filterList.add(
                        String.format(
                            Locale.US, "afade=t=out:st=%.2f:d=%.2f",
                            duration - fadeOut, fadeOut
                        )
                    )
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

                iEditMediaCallback!!.onCmdPlay(TextUtils.join(",", filterList))
            }

            override fun pause() {
                iEditMediaCallback?.pausePreview()
            }
        }


    constructor(
        callback: EditMediaFragment.IEditMediaCallback?,
        entityAudio: EntityAudio?
    ) : this() {
        this.iEditMediaCallback = callback
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
        val inflate = FragmentReverbePresetBinding.inflate(inflater, container, false)
        binding = inflate
        val root = inflate.root as LinearLayout

        if (iEditMediaCallback != null && entityAudio != null) {
            val recyclerView = root.findViewById<RecyclerView>(R.id.rv)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.setHasFixedSize(true)
            recyclerView.itemAnimator = null

            val reverbeList = ArrayList<Reverbe>()
            reverbeList.add(Reverbe(getString(R.string.reverb_normal), null))
            reverbeList.add(Reverbe(getString(R.string.reverb_masjid), "aecho=0.9:0.4:900|1800:0.20|0.15"))
            reverbeList.add(Reverbe(getString(R.string.reverb_masjid_2), "aecho=0.9:0.4:900:0.18"))
            reverbeList.add(Reverbe(getString(R.string.reverb_studio), "aecho=0.8:0.35:400|700:0.20|0.15"))
            reverbeList.add(Reverbe(getString(R.string.reverb_quiet_room), "aecho=0.6:0.3:300:0.12"))
            reverbeList.add(Reverbe(getString(R.string.reverb_tiled_room), "aecho=0.9:0.4:600|1200:0.20|0.15"))
            reverbeList.add(Reverbe(getString(R.string.reverb_deep), "aecho=0.6:0.35:1000:0.20"))

            // TODO: ReverbeAdabter needs to be implemented
            recyclerView.adapter = ReverbeAdabter(
                reverbeList,
                iReverbPresetCallback,
                entityAudio!!.effectAudio.reverbPreset_index_list
            )

            root.findViewById<View>(R.id.btn_done).setOnClickListener {
                iEditMediaCallback?.pausePreview()
                iEditMediaCallback?.onDone()
            }

            root.findViewById<View>(R.id.btn_appl_all).setOnClickListener {
                applyAll()
            }
        }

        return root
    }

    private fun applyAll() {
        val effectAudio = entityAudio!!.effectAudio
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

        iEditMediaCallback?.updateEntity(EffectAudioType.REVERB, entityAudio!!)
        iEditMediaCallback?.onCmdAll(effectAudio)
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
        iEditMediaCallback?.pausePreview()
        iReverbPresetCallback = null
        super.onDestroyView()
        instance = null
        binding = null
    }
}
