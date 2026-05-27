package hazem.nurmontage.videoquran.fragment.audio_effect

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.databinding.FragmentVolumeBinding
import hazem.nurmontage.videoquran.entity_timeline.EntityAudio
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * Fragment for adjusting audio pitch with seekbar and preview.
 * Converted from original Java PitchFragment (147 lines).
 */
class PitchFragment() : Fragment() {

    private var binding: FragmentVolumeBinding? = null
    private var btnPreview: ImageButton? = null
    private var entityAudio: EntityAudio? = null
    // TODO: Replace with EditMediaFragment.IEditMediaCallback when written
    private var iVolumeCallback: Any? = null
    private var isPlay: Boolean = false
    private var tvProgress: TextCustumFont? = null
    private var volumeSeekBar: SeekBar? = null

    companion object {
        private var instance: PitchFragment? = null

        @JvmStatic
        fun getInstance(callback: Any, entityAudio: EntityAudio): PitchFragment {
            if (instance == null) {
                instance = PitchFragment(callback, entityAudio)
            }
            return instance!!
        }
    }

    constructor(callback: Any, entityAudio: EntityAudio) : this() {
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

        // Check if entity audio is available with a MediaPlayer
        val audio = entityAudio
        // TODO: Restore MediaPlayer check when EntityAudio has getMediaPlayer()
        // if (audio == null || audio.mediaPlayer == null) return root
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
        // TODO: Cast iVolumeCallback to EditMediaFragment.IEditMediaCallback when written
        // val callback = iVolumeCallback as? EditMediaFragment.IEditMediaCallback ?: return
        // callback.pausePreview()
        // callback.onDone()
    }

    private fun previewAudio() {
        isPlay = !isPlay
        // TODO: Cast iVolumeCallback to EditMediaFragment.IEditMediaCallback when written
        // val callback = iVolumeCallback as? EditMediaFragment.IEditMediaCallback ?: return
        // if (isPlay) {
        //     callback.startPreview()
        //     btnPreview?.setImageResource(R.drawable.pause_24px)
        // } else {
        //     callback.pausePreview()
        //     btnPreview?.setImageResource(R.drawable.play_arrow_24px)
        // }
    }

    private fun applyVolume() {
        // Calculate pitch factor (unused result in original Java, kept for parity)
        val pitchFactor = Math.pow(2.0, 1.0 / 12.0)
        // TODO: Cast iVolumeCallback to EditMediaFragment.IEditMediaCallback when written
        // val callback = iVolumeCallback as? EditMediaFragment.IEditMediaCallback ?: return
        // callback.onCmd("asetrate=44100*1.2,atempo=0.8333")
    }

    override fun onDestroyView() {
        // TODO: Cast iVolumeCallback to EditMediaFragment.IEditMediaCallback when written
        // val callback = iVolumeCallback as? EditMediaFragment.IEditMediaCallback
        // callback?.pausePreview()
        super.onDestroyView()
        instance = null
        binding = null
    }
}
