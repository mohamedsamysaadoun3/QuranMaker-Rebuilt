package hazem.nurmontage.videoquran.fragment.audio_effect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.databinding.FragmentVolumeBinding
import hazem.nurmontage.videoquran.entity_timeline.EntityAudio
import hazem.nurmontage.videoquran.fragment.EditMediaFragment
import hazem.nurmontage.videoquran.views.TextCustumFont

class PitchFragment() : Fragment() {

    private var binding: FragmentVolumeBinding? = null
    private var btnPreview: ImageButton? = null
    private var entityAudio: EntityAudio? = null
    private var iVolumeCallback: EditMediaFragment.IEditMediaCallback? = null
    private var isPlay: Boolean = false
    private var tvProgress: TextCustumFont? = null
    private var volumeSeekBar: SeekBar? = null

    companion object {
        private var instance: PitchFragment? = null

        @JvmStatic
        fun getInstance(callback: EditMediaFragment.IEditMediaCallback, entityAudio: EntityAudio): PitchFragment {
            if (instance == null) {
                instance = PitchFragment(callback, entityAudio)
            }
            return instance!!
        }
    }

    constructor(callback: EditMediaFragment.IEditMediaCallback, entityAudio: EntityAudio) : this() {
        iVolumeCallback = callback
        this.entityAudio = entityAudio
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflate = FragmentVolumeBinding.inflate(inflater, container, false)
        binding = inflate
        val root = inflate.root

        val audio = entityAudio
        if (audio == null) return root

        tvProgress = root.findViewById(R.id.tv_volume_size)
        volumeSeekBar = root.findViewById<SeekBar>(R.id.volumeSeekBar).also {
            it.max = 40
            it.progress = 20
        }

        volumeSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    tvProgress?.text = progress.toString()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (isPlay) {
                    previewAudio()
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                applyVolume()
            }
        })

        root.findViewById<View>(R.id.btn_done).setOnClickListener {
            done()
        }

        btnPreview = root.findViewById<ImageButton>(R.id.btn_play).also {
            it.setOnClickListener {
                previewAudio()
            }
        }

        return root
    }

    private fun done() {
        val callback = iVolumeCallback ?: return
        callback.pausePreview()
        callback.onDone()
    }

    private fun previewAudio() {
        isPlay = !isPlay
        val callback = iVolumeCallback ?: return
        if (isPlay) {
            callback.startPreview()
            btnPreview?.setImageResource(R.drawable.pause_24px)
        } else {
            callback.pausePreview()
            btnPreview?.setImageResource(R.drawable.play_arrow_24px)
        }
    }

    private fun applyVolume() {
        val seekProgress = volumeSeekBar?.progress ?: return
        val semitone = seekProgress - 20
        val factor = Math.pow(2.0, semitone / 12.0)
        val asetrate = 44100.0 * factor
        val atempo = 1.0 / factor
        val cmd = "asetrate=${asetrate},atempo=$atempo"
        iVolumeCallback?.onCmd(cmd)
    }

    override fun onDestroyView() {
        iVolumeCallback?.pausePreview()
        super.onDestroyView()
        instance = null
        binding = null
    }
}
