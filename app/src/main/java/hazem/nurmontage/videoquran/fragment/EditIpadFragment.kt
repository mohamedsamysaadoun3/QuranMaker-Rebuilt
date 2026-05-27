package hazem.nurmontage.videoquran.fragment

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.Utils.BillingPreferences
import hazem.nurmontage.videoquran.adabter.IpadAdabter
import hazem.nurmontage.videoquran.constant.IpadType
import hazem.nurmontage.videoquran.databinding.FragmentEditIpadBinding
import hazem.nurmontage.videoquran.model.Gradient
import hazem.nurmontage.videoquran.model.IpadItem
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * Fragment for selecting the iPad frame style and color/gradient.
 * Provides a horizontal list of iPad frame types (IPAD, CLASSIC, CASSET, RECT, etc.)
 * and a tab layout for switching between color and gradient selection.
 * Converted from EditIpadFragment.java (209 lines).
 */
class EditIpadFragment() : Fragment() {

    companion object {
        var instance: EditIpadFragment? = null

        fun getInstance(
            resources: Resources?,
            ipadType: Int,
            callback: IIpadEditCallback?,
            indexSelect: Int,
            isGradient: Boolean,
            isGlass: Boolean
        ): EditIpadFragment {
            if (instance == null) {
                instance = EditIpadFragment(resources, ipadType, callback, indexSelect, isGradient, isGlass)
            }
            return instance!!
        }
    }

    interface IIpadEditCallback {
        fun onCancel()
        fun onChangeType(type: Int)
        fun onClick(index: Int, color: Int)
        fun onClick(gradient: Gradient, index: Int)
        fun onDialogPremium()
        fun onDone()
        fun onGlassType(isGlass: Boolean)
    }

    private var fragmentBinding: FragmentEditIpadBinding? = null
    private var iIpadEditCallback: IIpadEditCallback? = null
    private var indexSelect: Int = 0
    private var ipadAdabter: IpadAdabter? = null
    private var ipadType: Int = 0
    private var isGlass: Boolean = false
    private var isGradient: Boolean = false
    private var mCurrentPosFragment: Int = 0
    private var resourcesRef: Resources? = null
    private var rvType: RecyclerView? = null


    constructor(
        resources: Resources?,
        ipadType: Int,
        callback: IIpadEditCallback?,
        indexSelect: Int,
        isGradient: Boolean,
        isGlass: Boolean
    ) : this() {
        this.iIpadEditCallback = callback
        this.ipadType = ipadType
        this.isGlass = isGlass
        this.resourcesRef = resources
        this.indexSelect = indexSelect
        this.isGradient = isGradient
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflate = FragmentEditIpadBinding.inflate(inflater, container, false)
        fragmentBinding = inflate
        val root = inflate.root as RelativeLayout

        rvType = root.findViewById(R.id.rv_type)

        val ipadItems = ArrayList<IpadItem>()
        ipadItems.add(IpadItem(R.drawable.ipad_t, IpadType.IPAD))
        ipadItems.add(IpadItem(R.drawable.ipad_unblur, IpadType.IPAD_UNBLUR))
        ipadItems.add(IpadItem(R.drawable.ipad_classic, IpadType.IPAD_CLASSIC))
        ipadItems.add(IpadItem(R.drawable.ipad_neomorphic, IpadType.IPAD_NEOMORPHIC))
        ipadItems.add(IpadItem(R.drawable.ipad_caset, IpadType.CASSET))
        ipadItems.add(IpadItem(R.drawable.ipad_caset_img, IpadType.CASSET_IMG))
        ipadItems.add(IpadItem(R.drawable.ipad_caset_img_blur, IpadType.CASSET_IMG_BLUR))
        ipadItems.add(IpadItem(R.drawable.ipad_rect, IpadType.RECT))
        ipadItems.add(IpadItem(R.drawable.ipad_rect_round, IpadType.ROUND_RECT))
        ipadItems.add(IpadItem(R.drawable.ipad_bottom_rect, IpadType.BOTTOM_RECT))
        ipadItems.add(IpadItem(R.drawable.ipad_layer_black, IpadType.BLACK_LAYER))
        ipadItems.add(IpadItem(R.drawable.ipad_gradient, IpadType.GRADIENT))
        ipadItems.add(IpadItem(R.drawable.ipad_mask, IpadType.MASK_BRUSH))
        ipadItems.add(IpadItem(R.drawable.ipad_blue_type, IpadType.BLUE_TYPE))
        ipadItems.add(IpadItem(R.drawable.ic_heart_ipad, IpadType.HEART))
        ipadItems.add(IpadItem(R.drawable.ic_battery, IpadType.BATTERY))

        val posSelect = getPosSelect(ipadType, ipadItems)

        // TODO: IpadAdabter needs to be implemented
        ipadAdabter = IpadAdabter(
            BillingPreferences.isSubscribed(requireContext()),
            posSelect,
            ipadType,
            iIpadEditCallback,
            ipadItems,
            isGlass
        )

        rvType?.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
            itemAnimator = null
            adapter = ipadAdabter
        }

        if (posSelect > 3) {
            try {
                rvType?.scrollToPosition(posSelect - 3)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        root.findViewById<View>(R.id.btn_done).setOnClickListener {
            iIpadEditCallback?.onDone()
        }

        initTab(root)

        return root
    }

    fun scrollToSelectedPosition() {
        try {
            val layoutManager = rvType?.layoutManager as? LinearLayoutManager ?: return
            layoutManager.scrollToPositionWithOffset(
                ipadAdabter!!.getPos_select(),
                rvType!!.width / 2 - 50
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addCustomViewToTab(tab: TabLayout.Tab) {
        val customView = layoutInflater.inflate(R.layout.layout_tablayout, null as ViewGroup?)
        customView.findViewById<TextCustumFont>(R.id.name).text = tab.text.toString()
        tab.customView = customView
    }

    private fun initTab(view: View) {
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)

        val tabColor = tabLayout.newTab()
        tabColor.text = resourcesRef?.getString(R.string.color)
        addCustomViewToTab(tabColor)

        val tabGradient = tabLayout.newTab()
        tabGradient.text = resourcesRef?.getString(R.string.gradient)
        addCustomViewToTab(tabGradient)

        if (isGradient) {
            tabLayout.addTab(tabColor, false)
            tabLayout.addTab(tabGradient, true)
        } else {
            tabLayout.addTab(tabColor, true)
            tabLayout.addTab(tabGradient, false)
        }

        tabLayout.tabMode = TabLayout.MODE_FIXED
        mCurrentPosFragment = if (isGradient) 1 else 0

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {}
            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabSelected(tab: TabLayout.Tab) {
                if (mCurrentPosFragment != tab.position) {
                    mCurrentPosFragment = tab.position
                    val transaction = childFragmentManager.beginTransaction()
                    transaction.replace(R.id.container, getFragment(mCurrentPosFragment))
                        .addToBackStack(null).commit()
                }
            }
        })

        childFragmentManager.beginTransaction()
            .replace(R.id.container, getFragment(mCurrentPosFragment))
            .addToBackStack(null)
            .commit()

        indexSelect = -1
    }

    private fun getFragment(position: Int): Fragment {
        // TODO: GradientFragment and ColorsFragment need to be implemented
        return if (position == 1) {
            GradientFragment.getInstance(iIpadEditCallback as Any, indexSelect)
        } else {
            ColorsFragment.getInstance(iIpadEditCallback as Any, indexSelect)
        }
    }

    private fun getPosSelect(ipadType: Int, list: List<IpadItem>): Int {
        for (i in list.indices) {
            if (list[i].ipadType.ordinal == ipadType) {
                return i
            }
        }
        return 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        instance = null
        fragmentBinding = null
        iIpadEditCallback = null
    }
}
