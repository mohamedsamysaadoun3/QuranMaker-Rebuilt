package hazem.nurmontage.videoquran.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.adabter.ColorAdabter
import hazem.nurmontage.videoquran.common.Common
import hazem.nurmontage.videoquran.databinding.FragmentColorsBinding

class ColorsFragment() : Fragment() {

    private var adapter: ColorAdabter? = null
    private var binding: FragmentColorsBinding? = null
    private var iColor: ColorAdabter.IColor? = null
    private var iIpadEditCallback: EditIpadFragment.IIpadEditCallback? = null
    private var index: Int = 0
    private var recyclerView: RecyclerView? = null

    companion object {
        private var instance: ColorsFragment? = null

        @JvmStatic
        fun getInstance(iIpadEditCallback: EditIpadFragment.IIpadEditCallback, index: Int): ColorsFragment {
            if (instance == null) {
                instance = ColorsFragment(iIpadEditCallback, index)
            }
            return instance!!
        }
    }

    constructor(iIpadEditCallback: EditIpadFragment.IIpadEditCallback, index: Int) : this() {
        this.iIpadEditCallback = iIpadEditCallback
        this.index = index
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflate = FragmentColorsBinding.inflate(inflater, container, false)
        binding = inflate
        val root = inflate.root
        recyclerView = root.findViewById(R.id.rv_color)

        iColor = ColorAdabter.IColor { color, position ->
            scrollToSelectedPosition()
            iIpadEditCallback?.onClick(position, color)
        }
        val colorAdapter = ColorAdabter(iColor!!, Common.MUSLIM_COLORS, index)
        adapter = colorAdapter
        recyclerView?.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            itemAnimator = null
            setHasFixedSize(true)
            this.adapter = colorAdapter
        }
        try {
            if (index > 3) {
                recyclerView?.scrollToPosition(index - 3)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return root
    }

    fun scrollToSelectedPosition() {
        val layoutManager = recyclerView?.layoutManager as? LinearLayoutManager ?: return
        val pos = adapter?.getPos_select() ?: return
        layoutManager.scrollToPositionWithOffset(pos, (recyclerView!!.width / 2) - 50)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        instance = null
        binding = null
        iColor = null
        adapter = null
    }
}
