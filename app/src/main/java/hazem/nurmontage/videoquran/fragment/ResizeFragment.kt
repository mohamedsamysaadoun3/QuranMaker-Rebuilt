package hazem.nurmontage.videoquran.fragment

import android.app.Activity
import android.content.res.Resources
import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.Utils.ScreenUtils
import hazem.nurmontage.videoquran.Utils.Utils
import hazem.nurmontage.videoquran.common.DataDimension
import hazem.nurmontage.videoquran.databinding.FragmentResizeBinding
import hazem.nurmontage.videoquran.model.ItemDimension

/**
 * Fragment for selecting video output dimension/resize preset.
 * Converted from original Java ResizeFragment (119 lines).
 */
class ResizeFragment() : Fragment() {

    // TODO: Replace with actual adapter when DimensionAdabters is written
    // private var adabter: DimensionAdabters? = null
    private var adabterRef: Any? = null
    private var binding: FragmentResizeBinding? = null
    // TODO: Replace with actual callback type when DimensionAdabters is written
    // private var iDimensionCallback: DimensionAdabters.IDimensionCallback? = null
    private var iDimensionCallback: Any? = null
    private var posSelectResize: Int = -1
    private var recyclerView: RecyclerView? = null
    private var res: Resources? = null
    private var selectResize: String? = null


    companion object {
        private var instance: ResizeFragment? = null

        @JvmStatic
        fun getInstance(callback: Any, resources: Resources, selectResize: String): ResizeFragment {
            if (instance == null) {
                instance = ResizeFragment(callback, resources, selectResize)
            }
            return instance!!
        }
    }

    constructor(callback: Any, resources: Resources, selectResize: String) : this() {
        iDimensionCallback = callback
        this.selectResize = selectResize
        res = resources
    }

    fun scrollToSelectedPosition() {
        try {
            val layoutManager = recyclerView?.layoutManager as? LinearLayoutManager ?: return
            // TODO: Use adapter method when DimensionAdabters is implemented
            // layoutManager.scrollToPositionWithOffset(
            //     (adabterRef as DimensionAdabters).selected,
            //     (recyclerView!!.width / 2) - 50
            // )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflate = FragmentResizeBinding.inflate(inflater, container, false)
        binding = inflate
        val root = inflate.root

        if (res != null && iDimensionCallback != null) {
            root.findViewById<View>(R.id.btn_done).setOnClickListener {
                // TODO: Call iDimensionCallback.done() when DimensionAdabters is implemented
            }

            recyclerView = root.findViewById(R.id.rv)
            recyclerView?.setHasFixedSize(true)
            recyclerView?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            recyclerView?.itemAnimator = null

            val allDimensions = DataDimension.getALl(res!!)
            val dimensionPairs = getListDimension(activity, allDimensions)

            // TODO: Uncomment when DimensionAdabters is implemented
            // val adapter = DimensionAdabters(
            //     allDimensions,
            //     iDimensionCallback as DimensionAdabters.IDimensionCallback,
            //     dimensionPairs,
            //     posSelectResize
            // )
            // adabterRef = adapter
            // recyclerView?.adapter = adapter
            // if (posSelectResize > 0) {
            //     recyclerView?.scrollToPosition(posSelectResize - 1)
            // } else {
            //     recyclerView?.scrollToPosition(posSelectResize)
            // }
        }
        return root
    }

    /**
     * Computes preview dimension pairs for each item and finds the selected index.
     */
    fun getListDimension(activity: Activity?, list: List<ItemDimension>): List<Pair<Int, Int>> {
        val screenWidth = (ScreenUtils.getScreenWidth(activity!!) * 0.27f).toInt()
        val result = mutableListOf<Pair<Int, Int>>()
        for (i in list.indices) {
            val item = list[i]
            if (item.id == selectResize) {
                posSelectResize = i
            }
            result.add(Utils.getDimension(item.resizeType, screenWidth))
        }
        return result
    }

    override fun onDestroyView() {
        iDimensionCallback = null
        instance = null
        binding?.root?.removeAllViews()
        binding = null
        super.onDestroyView()
    }
}
