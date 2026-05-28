package hazem.nurmontage.videoquran.fragment

import android.app.Activity
import android.content.res.Resources
import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.Utils.ScreenUtils
import hazem.nurmontage.videoquran.Utils.Utils
import hazem.nurmontage.videoquran.adabter.DimensionAdabters
import hazem.nurmontage.videoquran.common.DataDimension
import hazem.nurmontage.videoquran.databinding.FragmentResizeBinding
import hazem.nurmontage.videoquran.model.ItemDimension

class ResizeFragment() : Fragment() {

    private var adapter: DimensionAdabters? = null
    private var binding: FragmentResizeBinding? = null
    private var iDimensionCallback: DimensionAdabters.IDimensionCallback? = null
    private var posSelectResize: Int = -1
    private var recyclerView: RecyclerView? = null
    private var res: Resources? = null
    private var selectResize: String? = null

    companion object {
        private var instance: ResizeFragment? = null

        @JvmStatic
        fun getInstance(callback: DimensionAdabters.IDimensionCallback, resources: Resources, selectResize: String): ResizeFragment {
            if (instance == null) {
                instance = ResizeFragment(callback, resources, selectResize)
            }
            return instance!!
        }
    }

    constructor(callback: DimensionAdabters.IDimensionCallback, resources: Resources, selectResize: String) : this() {
        iDimensionCallback = callback
        this.selectResize = selectResize
        res = resources
    }

    fun scrollToSelectedPosition() {
        try {
            val layoutManager = recyclerView?.layoutManager as? LinearLayoutManager ?: return
            val pos = adapter?.getSelected() ?: return
            layoutManager.scrollToPositionWithOffset(pos, (recyclerView!!.width / 2) - 50)
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
                iDimensionCallback?.done()
            }

            recyclerView = root.findViewById(R.id.rv)
            recyclerView?.setHasFixedSize(true)
            recyclerView?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            recyclerView?.itemAnimator = null

            val allDimensions = DataDimension.getALl(res!!)
            val dimensionPairs = getListDimension(activity, allDimensions)

            val dimAdapter = DimensionAdabters(
                allDimensions,
                iDimensionCallback,
                dimensionPairs,
                posSelectResize
            )
            adapter = dimAdapter
            recyclerView?.adapter = dimAdapter
            if (posSelectResize > 0) {
                recyclerView?.scrollToPosition(posSelectResize - 1)
            } else {
                recyclerView?.scrollToPosition(posSelectResize)
            }
        }
        return root
    }

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
        adapter?.clear()
        adapter = null
        instance = null
        binding?.root?.removeAllViews()
        binding = null
        super.onDestroyView()
    }
}
