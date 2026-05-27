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
 * Fragment for editing a Bismilah entity — provides color, delete, split, and animation options.
 * Hides buttons not relevant for Bismilah (duplicate, font, icon, edit, show-left, show-right, cut).
 * Converted from EditBismilahEntityFragment.java (209 lines).
 */
class EditBismilahEntityFragment() : Fragment() {

    companion object {
        var instance: EditBismilahEntityFragment? = null

        fun getInstance(
            callback: IBismilahEntityCallback?,
            resources: Resources?,
            entity: Entity?,
            posCursor: Float
        ): EditBismilahEntityFragment {
            if (instance == null) {
                instance = EditBismilahEntityFragment(callback, resources, entity, posCursor)
            }
            return instance!!
        }
    }

    interface IBismilahEntityCallback {
        fun fromNow()
        fun fromTheStart()
        fun onAnim()
        fun onColor()
        fun onDelete()
        fun onDone()
        fun untilNow()
        fun untilTheEnd()
        fun update()
        fun updateAya(color: Int)
        fun updatePreset(preset: AyaTextPreset)
    }

    private var btnDelete: LinearLayout? = null
    private var btnFromNow: LinearLayout? = null
    private var btnUntilNow: LinearLayout? = null
    private var entitySelect: Entity? = null
    private var fragmentBinding: FragmentEditEntityBinding? = null
    private var iEditEntityCallback: IBismilahEntityCallback? = null
    private var ivFromNow: ImageView? = null
    private var ivUntilNow: ImageView? = null
    private var posCursor: Float = 0f
    private var resourcesRef: Resources? = null
    private var tvFromNow: TextCustumFont? = null
    private var tvUntilNow: TextCustumFont? = null


    constructor(
        callback: IBismilahEntityCallback?,
        resources: Resources?,
        entity: Entity?,
        posCursor: Float
    ) : this() {
        this.iEditEntityCallback = callback
        this.resourcesRef = resources
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
        val root = inflate.root as RelativeLayout

        if (iEditEntityCallback != null && resourcesRef != null) {
            // Hide buttons not relevant for Bismilah
            root.findViewById<View>(R.id.btn_duplicate).visibility = View.GONE
            root.findViewById<View>(R.id.btn_font).visibility = View.GONE
            root.findViewById<View>(R.id.btn_icon).visibility = View.GONE
            root.findViewById<View>(R.id.btn_edit).visibility = View.GONE
            root.findViewById<View>(R.id.btn_show_left).visibility = View.GONE
            root.findViewById<View>(R.id.btn_show_right).visibility = View.GONE
            root.findViewById<View>(R.id.btn_cut).visibility = View.GONE

            ivFromNow = root.findViewById(R.id.iv_from_now)
            ivUntilNow = root.findViewById(R.id.iv_until_now)

            (root.findViewById<TextCustumFont>(R.id.tv_delete)).text =
                resourcesRef?.getString(R.string.delete)
            (root.findViewById<TextCustumFont>(R.id.tv_color)).text =
                resourcesRef?.getString(R.string.color)

            root.findViewById<View>(R.id.btn_color).setOnClickListener {
                iEditEntityCallback?.onColor()
            }

            val deleteBtn = root.findViewById<LinearLayout>(R.id.btn_delete)
            btnDelete = deleteBtn
            deleteBtn.setOnClickListener {
                iEditEntityCallback?.onDelete()
            }

            val fromNowText = root.findViewById<TextCustumFont>(R.id.tv_from_now)
            tvFromNow = fromNowText
            fromNowText.text = resourcesRef?.getString(R.string.from_now)

            (root.findViewById<TextCustumFont>(R.id.tv_from_the_start)).text =
                resourcesRef?.getString(R.string.from_the_start)

            val untilNowText = root.findViewById<TextCustumFont>(R.id.tv_until_now)
            tvUntilNow = untilNowText
            untilNowText.text = resourcesRef?.getString(R.string.until_now)

            (root.findViewById<TextCustumFont>(R.id.tv_until_the_end)).text =
                resourcesRef?.getString(R.string.until_the_end)

            (root.findViewById<TextCustumFont>(R.id.tv_anim)).text =
                resourcesRef?.getString(R.string.animtion)

            val fromNowBtn = root.findViewById<LinearLayout>(R.id.btn_from_now)
            btnFromNow = fromNowBtn
            fromNowBtn.setOnClickListener {
                iEditEntityCallback?.fromNow()
            }

            root.findViewById<View>(R.id.btn_from_the_start).setOnClickListener {
                iEditEntityCallback?.fromTheStart()
            }

            val untilNowBtn = root.findViewById<LinearLayout>(R.id.btn_until_now)
            btnUntilNow = untilNowBtn
            untilNowBtn.setOnClickListener {
                iEditEntityCallback?.untilNow()
            }

            root.findViewById<View>(R.id.btn_until_the_end).setOnClickListener {
                iEditEntityCallback?.untilTheEnd()
            }

            root.findViewById<View>(R.id.btn_anim).setOnClickListener {
                iEditEntityCallback?.onAnim()
            }

            checkSplitEntity(entitySelect, posCursor)
        }

        return root
    }

    fun checkSplitEntity(entity: Entity?, cursorPos: Float) {
        if (entity == null) return
        try {
            if (entity.getRect().right < cursorPos) {
                tvFromNow?.setTextColor(-8355712) // 0xFF808080
                ivFromNow?.setColorFilter(-8355712, PorterDuff.Mode.SRC_IN)
                btnFromNow?.isClickable = false
            } else {
                btnFromNow?.isClickable = true
                tvFromNow?.setTextColor(-1)
                ivFromNow?.setColorFilter(-1, PorterDuff.Mode.SRC_IN)
            }
            if (entity.getRect().left > cursorPos) {
                tvUntilNow?.setTextColor(-8355712)
                ivUntilNow?.setColorFilter(-8355712, PorterDuff.Mode.SRC_IN)
                btnUntilNow?.isClickable = false
            } else {
                btnUntilNow?.isClickable = true
                tvUntilNow?.setTextColor(-1)
                ivUntilNow?.setColorFilter(-1, PorterDuff.Mode.SRC_IN)
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
