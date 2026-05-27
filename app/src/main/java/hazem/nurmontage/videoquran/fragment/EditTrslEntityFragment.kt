package hazem.nurmontage.videoquran.fragment

import android.content.res.Resources
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.constant.AyaTextPreset
import hazem.nurmontage.videoquran.databinding.FragmentEditEntityBinding
import hazem.nurmontage.videoquran.entity_timeline.Entity
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * Fragment for editing translation entity on the timeline.
 * Shows options: color, delete, cut, edit, from-now, until-now, duplicate.
 * Hides font, icon, and animation buttons (not applicable to translation).
 * Converted from Java decompiled source.
 */
class EditTrslEntityFragment() : Fragment() {

    companion object {
        var instance: EditTrslEntityFragment? = null

        fun getInstance(
            iEditEntityCallback: IEditEntityCallback,
            resources: Resources,
            entity: Entity,
            posCursor: Float
        ): EditTrslEntityFragment {
            if (instance == null) {
                instance = EditTrslEntityFragment(iEditEntityCallback, resources, entity, posCursor)
            }
            return instance!!
        }

        private const val COLOR_DISABLED = -8355712  // 0xFF808080 gray
        private const val COLOR_ENABLED = -1          // 0xFFFFFFFF white
    }

    interface IEditEntityCallback {
        fun fromNow()
        fun fromTheStart()
        fun onAnim()
        fun onColor()
        fun onCut()
        fun onDelete()
        fun onDone()
        fun onDuplicate()
        fun onEdit()
        fun onFont()
        fun onIcon()
        fun untilNow()
        fun untilTheEnd()
        fun updateAya(i: Int)
        fun updatePreset(ayaTextPreset: AyaTextPreset)
        fun updateTrsl(i: Int)
    }

    private var btnCut: LinearLayout? = null
    private var btnFromNow: LinearLayout? = null
    private var btnUntilNow: LinearLayout? = null
    private var entitySelect: Entity? = null
    private var fragmentBinding: FragmentEditEntityBinding? = null
    private var iEditEntityCallback: IEditEntityCallback? = null
    private var ivCut: ImageView? = null
    private var ivFromNow: ImageView? = null
    private var ivUntilNow: ImageView? = null
    private var posCursor: Float = 0f
    private var res: Resources? = null
    private var tvCut: TextCustumFont? = null
    private var tvFromNow: TextCustumFont? = null
    private var tvUntilNow: TextCustumFont? = null


    constructor(
        iEditEntityCallback: IEditEntityCallback,
        resources: Resources,
        entity: Entity,
        posCursor: Float
    ) : this() {
        this.iEditEntityCallback = iEditEntityCallback
        this.res = resources
        this.entitySelect = entity
        this.posCursor = posCursor
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflate = FragmentEditEntityBinding.inflate(inflater, container, false)
        fragmentBinding = inflate
        val root: RelativeLayout = inflate.root

        if (iEditEntityCallback != null && res != null) {
            ivCut = root.findViewById(R.id.iv_cut)
            ivFromNow = root.findViewById(R.id.iv_from_now)
            ivUntilNow = root.findViewById(R.id.iv_until_now)

            (root.findViewById<View>(R.id.tv_delete) as TextCustumFont)
                .text = res!!.getString(R.string.delete)

            val tvCutView: TextCustumFont = root.findViewById(R.id.tv_cut)
            tvCut = tvCutView
            tvCutView.text = res!!.getString(R.string.cut)

            (root.findViewById<View>(R.id.tv_edit) as TextCustumFont)
                .text = res!!.getString(R.string.edit)
            (root.findViewById<View>(R.id.tv_color) as TextCustumFont)
                .text = res!!.getString(R.string.color)

            root.findViewById<View>(R.id.btn_color).setOnClickListener {
                iEditEntityCallback?.onColor()
            }
            root.findViewById<View>(R.id.btn_delete).setOnClickListener {
                iEditEntityCallback?.onDelete()
            }

            val cutBtn: LinearLayout = root.findViewById(R.id.btn_cut)
            btnCut = cutBtn
            cutBtn.setOnClickListener {
                iEditEntityCallback?.onCut()
            }

            root.findViewById<View>(R.id.btn_edit).setOnClickListener {
                iEditEntityCallback?.onEdit()
            }

            val fromNowView: TextCustumFont = root.findViewById(R.id.tv_from_now)
            tvFromNow = fromNowView
            fromNowView.text = res!!.getString(R.string.from_now)

            (root.findViewById<View>(R.id.tv_from_the_start) as TextCustumFont)
                .text = res!!.getString(R.string.from_the_start)

            val untilNowView: TextCustumFont = root.findViewById(R.id.tv_until_now)
            tvUntilNow = untilNowView
            untilNowView.text = res!!.getString(R.string.until_now)

            (root.findViewById<View>(R.id.tv_until_the_end) as TextCustumFont)
                .text = res!!.getString(R.string.until_the_end)
            (root.findViewById<View>(R.id.tv_duplicate) as TextCustumFont)
                .text = res!!.getString(R.string.duplicate)

            val fromNowBtn: LinearLayout = root.findViewById(R.id.btn_from_now)
            btnFromNow = fromNowBtn
            fromNowBtn.setOnClickListener {
                iEditEntityCallback?.fromNow()
            }

            root.findViewById<View>(R.id.btn_from_the_start).setOnClickListener {
                iEditEntityCallback?.fromTheStart()
            }

            val untilNowBtn: LinearLayout = root.findViewById(R.id.btn_until_now)
            btnUntilNow = untilNowBtn
            untilNowBtn.setOnClickListener {
                iEditEntityCallback?.untilNow()
            }

            root.findViewById<View>(R.id.btn_until_the_end).setOnClickListener {
                iEditEntityCallback?.untilTheEnd()
            }

            root.findViewById<View>(R.id.btn_duplicate).setOnClickListener {
                iEditEntityCallback?.onDuplicate()
            }

            // Hide font, icon, and anim buttons (not applicable to translation entity)
            root.findViewById<View>(R.id.btn_font).visibility = View.GONE
            root.findViewById<View>(R.id.btn_icon).visibility = View.GONE
            root.findViewById<View>(R.id.btn_anim).visibility = View.GONE

            val btnShowLeft: ImageView = root.findViewById(R.id.btn_show_left)
            val btnShowRight: ImageView = root.findViewById(R.id.btn_show_right)
            btnShowLeft.visibility = View.GONE
            btnShowRight.visibility = View.GONE

            checkSplitEntity(entitySelect, posCursor)
        }

        return root
    }

    fun checkSplitEntity(entity: Entity?, posCursor: Float) {
        if (entity == null) return
        try {
            val rect = entity.getRect()
            if (rect.right < posCursor) {
                tvFromNow?.setTextColor(COLOR_DISABLED)
                ivFromNow?.setColorFilter(COLOR_DISABLED, PorterDuff.Mode.SRC_IN)
                btnFromNow?.isClickable = false
            } else {
                btnFromNow?.isClickable = true
                tvFromNow?.setTextColor(COLOR_ENABLED)
                ivFromNow?.setColorFilter(COLOR_ENABLED, PorterDuff.Mode.SRC_IN)
            }

            if (rect.left > posCursor) {
                tvUntilNow?.setTextColor(COLOR_DISABLED)
                ivUntilNow?.setColorFilter(COLOR_DISABLED, PorterDuff.Mode.SRC_IN)
                btnUntilNow?.isClickable = false
            } else {
                btnUntilNow?.isClickable = true
                tvUntilNow?.setTextColor(COLOR_ENABLED)
                ivUntilNow?.setColorFilter(COLOR_ENABLED, PorterDuff.Mode.SRC_IN)
            }

            if (rect.left <= posCursor && rect.right >= posCursor) {
                btnCut?.isClickable = true
                tvCut?.setTextColor(COLOR_ENABLED)
                ivCut?.setColorFilter(COLOR_ENABLED, PorterDuff.Mode.SRC_IN)
            } else {
                tvCut?.setTextColor(COLOR_DISABLED)
                ivCut?.setColorFilter(COLOR_DISABLED, PorterDuff.Mode.SRC_IN)
                btnCut?.isClickable = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        fragmentBinding = null
        instance = null
        iEditEntityCallback = null
        super.onDestroyView()
    }
}
