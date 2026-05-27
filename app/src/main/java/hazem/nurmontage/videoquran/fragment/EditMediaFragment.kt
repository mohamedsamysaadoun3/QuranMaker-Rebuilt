package hazem.nurmontage.videoquran.fragment

import android.content.res.Resources
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.Utils.MyPrefereces
import hazem.nurmontage.videoquran.constant.EffectAudioType
import hazem.nurmontage.videoquran.databinding.FragmentEditMediaBinding
import hazem.nurmontage.videoquran.entity_timeline.EntityAudio
import hazem.nurmontage.videoquran.model.EffectAudio
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * Fragment for editing media (audio) entity on the timeline.
 * Shows audio effect options: enhance, reverb, echo, volume, fade, speed, noice.
 * Also includes delete, duplicate, and cut options.
 * Converted from Java decompiled source.
 */
class EditMediaFragment() : Fragment() {

    companion object {
        var instance: EditMediaFragment? = null

        fun getInstance(
            iEditMediaCallback: IEditMediaCallback,
            resources: Resources,
            entityAudio: EntityAudio,
            posCursor: Float
        ): EditMediaFragment {
            if (instance == null) {
                instance = EditMediaFragment(iEditMediaCallback, resources, entityAudio, posCursor)
            }
            return instance!!
        }

        private const val COLOR_DISABLED = -8355712
        private const val COLOR_ENABLED = -1
    }

    interface IEditMediaCallback {
        fun echoEffect()
        fun enhanceVoice()
        fun fadeffect()
        fun noice()
        fun onCmd(cmd: String)
        fun onCmdAll(effectAudio: EffectAudio)
        fun onCmdPlay(cmd: String)
        fun onCut()
        fun onDelete()
        fun onDone()
        fun onDuplicate()
        fun onReplace()
        fun pausePreview()
        fun pitchffect()
        fun reverbEffect()
        fun speedffect()
        fun startPreview()
        fun updateEntity(effectAudioType: EffectAudioType, entityAudio: EntityAudio)
        fun volumeEffect()
    }

    private var btnCut: LinearLayout? = null
    private var btnEcho: LinearLayout? = null
    private var btnEnhanceVoice: LinearLayout? = null
    private var btnFade: LinearLayout? = null
    private var btnRemoveNoice: LinearLayout? = null
    private var btnReverb: LinearLayout? = null
    private var btnSpeed: LinearLayout? = null
    private var btnVolume: LinearLayout? = null
    private var entitySelect: EntityAudio? = null
    private var fragmentBinding: FragmentEditMediaBinding? = null
    private var iEditMediaCallback: IEditMediaCallback? = null
    private var ivCut: ImageView? = null
    private var posCursor: Float = 0f
    private var resources: Resources? = null
    private var tvCut: TextCustumFont? = null


    constructor(
        iEditMediaCallback: IEditMediaCallback,
        resources: Resources,
        entityAudio: EntityAudio,
        posCursor: Float
    ) : this() {
        this.iEditMediaCallback = iEditMediaCallback
        this.resources = resources
        this.entitySelect = entityAudio
        this.posCursor = posCursor
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflate = FragmentEditMediaBinding.inflate(inflater, container, false)
        fragmentBinding = inflate
        val root: RelativeLayout = inflate.root

        if (iEditMediaCallback != null && resources != null && entitySelect != null) {
            ivCut = root.findViewById(R.id.iv_cut)

            val horizontalScrollView: HorizontalScrollView = root.findViewById(R.id.view_scroll)
            btnCut = root.findViewById(R.id.btn_cut)

            val scrollX = MyPrefereces.getScrollX(requireContext())
            if (scrollX != 0) {
                MyPrefereces.putScrollX(requireContext(), 0)
                horizontalScrollView.post {
                    horizontalScrollView.scrollTo(scrollX, 0)
                }
            }

            val btnShowLeft: ImageView = root.findViewById(R.id.btn_show_left)
            val btnShowRight: ImageView = root.findViewById(R.id.btn_show_right)

            horizontalScrollView.setOnScrollChangeListener { _, scrollX, _, _, _ ->
                try {
                    if (scrollX > (btnCut?.width ?: 0) * 0.3f) {
                        btnShowRight.visibility = View.GONE
                        btnShowLeft.visibility = View.VISIBLE
                    } else {
                        btnShowLeft.visibility = View.GONE
                        btnShowRight.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            (root.findViewById<View>(R.id.tv_enhance) as TextCustumFont)
                .text = resources!!.getString(R.string.enhance)
            (root.findViewById<View>(R.id.tv_delete) as TextCustumFont)
                .text = resources!!.getString(R.string.delete)
            (root.findViewById<View>(R.id.tv_duplicate) as TextCustumFont)
                .text = resources!!.getString(R.string.duplicate)
            (root.findViewById<View>(R.id.tv_volume) as TextCustumFont)
                .text = resources!!.getString(R.string.volume)
            (root.findViewById<View>(R.id.tv_reverbe) as TextCustumFont)
                .text = resources!!.getString(R.string.reverb)
            (root.findViewById<View>(R.id.tv_echo) as TextCustumFont)
                .text = resources!!.getString(R.string.echo)
            (root.findViewById<View>(R.id.tv_fade) as TextCustumFont)
                .text = resources!!.getString(R.string.fade)
            (root.findViewById<View>(R.id.tv_noice) as TextCustumFont)
                .text = resources!!.getString(R.string.noice)
            (root.findViewById<View>(R.id.tv_speed) as TextCustumFont)
                .text = resources!!.getString(R.string.speed)

            val tvCutView: TextCustumFont = root.findViewById(R.id.tv_cut)
            tvCut = tvCutView
            tvCutView.text = resources!!.getString(R.string.cut)

            root.findViewById<View>(R.id.btn_delete).setOnClickListener {
                iEditMediaCallback?.onDelete()
            }

            root.findViewById<View>(R.id.btn_duplicate).setOnClickListener {
                iEditMediaCallback?.onDuplicate()
            }

            val reverbBtn: LinearLayout = root.findViewById(R.id.btn_reverb)
            btnReverb = reverbBtn
            reverbBtn.setOnClickListener {
                iEditMediaCallback?.let { callback ->
                    MyPrefereces.putScrollX(requireContext(), horizontalScrollView.scrollX)
                    callback.reverbEffect()
                }
            }

            val enhanceBtn: LinearLayout = root.findViewById(R.id.btn_enhance_voice)
            btnEnhanceVoice = enhanceBtn
            enhanceBtn.setOnClickListener {
                iEditMediaCallback?.let { callback ->
                    MyPrefereces.putScrollX(requireContext(), horizontalScrollView.scrollX)
                    callback.enhanceVoice()
                }
            }

            val noiceBtn: LinearLayout = root.findViewById(R.id.btn_remove_noice)
            btnRemoveNoice = noiceBtn
            noiceBtn.setOnClickListener {
                iEditMediaCallback?.let { callback ->
                    MyPrefereces.putScrollX(requireContext(), horizontalScrollView.scrollX)
                    callback.noice()
                }
            }

            val echoBtn: LinearLayout = root.findViewById(R.id.btn_echo)
            btnEcho = echoBtn
            echoBtn.setOnClickListener {
                iEditMediaCallback?.let { callback ->
                    MyPrefereces.putScrollX(requireContext(), horizontalScrollView.scrollX)
                    callback.echoEffect()
                }
            }

            val volumeBtn: LinearLayout = root.findViewById(R.id.btn_volume)
            btnVolume = volumeBtn
            volumeBtn.setOnClickListener {
                iEditMediaCallback?.let { callback ->
                    MyPrefereces.putScrollX(requireContext(), horizontalScrollView.scrollX)
                    callback.volumeEffect()
                }
            }

            val fadeBtn: LinearLayout = root.findViewById(R.id.btn_fade)
            btnFade = fadeBtn
            fadeBtn.setOnClickListener {
                iEditMediaCallback?.let { callback ->
                    MyPrefereces.putScrollX(requireContext(), horizontalScrollView.scrollX)
                    callback.fadeffect()
                }
            }

            val speedBtn: LinearLayout = root.findViewById(R.id.btn_speed)
            btnSpeed = speedBtn
            speedBtn.setOnClickListener {
                iEditMediaCallback?.let { callback ->
                    MyPrefereces.putScrollX(requireContext(), horizontalScrollView.scrollX)
                    callback.speedffect()
                }
            }

            btnCut?.setOnClickListener {
                iEditMediaCallback?.onCut()
            }

            updateBtn()
            initCheckSplit(entitySelect, posCursor)
        }

        return root
    }

    fun updateBtn() {
        try {
            val effectAudio = entitySelect?.effectAudio ?: return

            if (effectAudio.reverbPreset != null) {
                btnReverb?.setBackgroundResource(R.drawable.bg_item_effect)
            } else {
                btnReverb?.setBackgroundColor(0)
            }

            if (effectAudio.isEnhance) {
                btnEnhanceVoice?.setBackgroundResource(R.drawable.bg_item_effect)
            } else {
                btnEnhanceVoice?.setBackgroundColor(0)
            }

            if (effectAudio.isRemoveNoice) {
                btnRemoveNoice?.setBackgroundResource(R.drawable.bg_item_effect)
            } else {
                btnRemoveNoice?.setBackgroundColor(0)
            }

            if (effectAudio.decays != 0 && effectAudio.delays != 0 && effectAudio.volume_echo != 0) {
                btnEcho?.setBackgroundResource(R.drawable.bg_item_effect)
            } else {
                btnEcho?.setBackgroundColor(0)
            }

            if (effectAudio.volume != 1.0f) {
                btnVolume?.setBackgroundResource(R.drawable.bg_item_effect)
            } else {
                btnVolume?.setBackgroundColor(0)
            }

            if (effectAudio.speed != 1.0f) {
                btnSpeed?.setBackgroundResource(R.drawable.bg_item_effect)
            } else {
                btnSpeed?.setBackgroundColor(0)
            }

            if (effectAudio.fade_in > 0 && effectAudio.fade_out > 0) {
                btnFade?.setBackgroundResource(R.drawable.bg_item_effect)
            } else {
                btnFade?.setBackgroundColor(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun initCheckSplit(entityAudio: EntityAudio?, posCursor: Float) {
        try {
            if (entityAudio != null) {
                val rect = entityAudio.getRect()
                if (rect.left <= posCursor && rect.right >= posCursor) {
                    btnCut?.isClickable = true
                    tvCut?.setTextColor(COLOR_ENABLED)
                    ivCut?.setColorFilter(COLOR_ENABLED, PorterDuff.Mode.SRC_IN)
                }
                // Always ends up disabled per original Java logic
                tvCut?.setTextColor(COLOR_DISABLED)
                ivCut?.setColorFilter(COLOR_DISABLED, PorterDuff.Mode.SRC_IN)
                btnCut?.isClickable = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun checkSplit(entityAudio: EntityAudio?, posCursor: Float) {
        if (entityAudio == null) return
        entitySelect = entityAudio
        updateBtn()
        try {
            val rect = entityAudio.getRect()
            if (rect.left <= posCursor && rect.right >= posCursor) {
                btnCut?.isClickable = true
                tvCut?.setTextColor(COLOR_ENABLED)
                ivCut?.setColorFilter(COLOR_ENABLED, PorterDuff.Mode.SRC_IN)
            }
            // Always ends up disabled per original Java logic
            tvCut?.setTextColor(COLOR_DISABLED)
            ivCut?.setColorFilter(COLOR_DISABLED, PorterDuff.Mode.SRC_IN)
            btnCut?.isClickable = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        fragmentBinding = null
        instance = null
        iEditMediaCallback = null
        super.onDestroyView()
    }
}
