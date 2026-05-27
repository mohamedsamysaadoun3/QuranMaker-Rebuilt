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
import hazem.nurmontage.videoquran.databinding.FragmentEditMediaMultipleBinding
import hazem.nurmontage.videoquran.entity_timeline.Entity
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * Fragment for editing multiple selected entities (delete, cut).
 * Converted from original Java EditMultipleEntityFragment (106 lines).
 */
class EditMultipleEntityFragment() : Fragment() {

    companion object {
        var instance: EditMultipleEntityFragment? = null

        const val GRAY_COLOR = -8355712  // 0xFF808080
        const val WHITE_COLOR = -1  // 0xFFFFFFFF
    }

    private var btnCut: LinearLayout? = null
    private var countSelect: Int = 0
    private var fragmentBinding: FragmentEditMediaMultipleBinding? = null
    private var iEditMediaCallback: IEditMultipleCallback? = null
    private var ivCut: ImageView? = null
    private var resourcesRef: Resources? = null
    private var tvCut: TextCustumFont? = null
    private var tvDelete: TextCustumFont? = null

    interface IEditMultipleCallback {
        fun onDelete()
    }

    fun setCountSelect(count: Int) {
        // No-op in original
    }

    

    constructor(callback: IEditMultipleCallback, resources: Resources, count: Int) : this() {
        iEditMediaCallback = callback
        resourcesRef = resources
        countSelect = count
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflate = FragmentEditMediaMultipleBinding.inflate(inflater, container, false)
        fragmentBinding = inflate
        val root = inflate.root

        if (iEditMediaCallback != null && resourcesRef != null) {
            ivCut = root.findViewById(R.id.iv_cut)
            ivCut?.setColorFilter(GRAY_COLOR, PorterDuff.Mode.SRC_IN)

            tvDelete = root.findViewById(R.id.tv_delete)
            tvDelete?.text = resourcesRef!!.getString(R.string.delete)

            tvCut = root.findViewById(R.id.tv_cut)
            tvCut?.text = resourcesRef!!.getString(R.string.cut)
            tvCut?.setTextColor(GRAY_COLOR)

            root.findViewById<View>(R.id.btn_delete).setOnClickListener {
                iEditMediaCallback?.onDelete()
            }
        }
        return root
    }

    /**
     * Checks whether the cursor position falls within the entity's rect
     * and enables/disables the cut button accordingly.
     */
    fun checkSplit(entity: Entity?, cursorPos: Float) {
        if (entity == null) return
        try {
            if (entity.rectF.left <= cursorPos && entity.rectF.right >= cursorPos) {
                btnCut?.isClickable = true
                tvCut?.setTextColor(WHITE_COLOR)
                ivCut?.setColorFilter(WHITE_COLOR, PorterDuff.Mode.SRC_IN)
            }
            tvCut?.setTextColor(GRAY_COLOR)
            ivCut?.setColorFilter(GRAY_COLOR, PorterDuff.Mode.SRC_IN)
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
