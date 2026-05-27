package hazem.nurmontage.videoquran.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.common.Common
import hazem.nurmontage.videoquran.databinding.FragmentColorsBinding

/**
 * Fragment displaying a horizontal color picker RecyclerView for ipad background colors.
 * Converted from original Java ColorsFragment (83 lines).
 */
class ColorsFragment() : Fragment() {

    // TODO: Replace with actual adapter when ColorAdabter is written
    // private var adapter: ColorAdabter? = null
    private var binding: FragmentColorsBinding? = null
    private var iColor: Any? = null // Will be ColorAdabter.IColor
    private var iIpadEditCallback: Any? = null // Will be EditIpadFragment.IIpadEditCallback
    private var index: Int = 0
    private var recyclerView: RecyclerView? = null
    private var adapterRef: Any? = null // Will be ColorAdabter

    companion object {
        private var instance: ColorsFragment? = null

        @JvmStatic
        fun getInstance(iIpadEditCallback: Any, index: Int): ColorsFragment {
            if (instance == null) {
                instance = ColorsFragment(iIpadEditCallback, index)
            }
            return instance!!
        }
    }

    constructor(iIpadEditCallback: Any, index: Int) : this() {
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

        // TODO: Uncomment when ColorAdabter is implemented
        // iColor = object : ColorAdabter.IColor {
        //     override fun onColor(color: Int, position: Int) {
        //         scrollToSelectedPosition()
        //         (iIpadEditCallback as? EditIpadFragment.IIpadEditCallback)?.onClick(color, position)
        //     }
        // }
        // val adapter = ColorAdabter(iColor as ColorAdabter.IColor, Common.MUSLIM_COLORS, index)
        // adapterRef = adapter
        // recyclerView?.apply {
        //     layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        //     itemAnimator = null
        //     setHasFixedSize(true)
        //     this.adapter = adapter
        // }
        // try {
        //     if (index > 3) {
        //         recyclerView?.scrollToPosition(index - 3)
        //     }
        // } catch (e: Exception) {
        //     e.printStackTrace()
        // }

        return root
    }

    fun scrollToSelectedPosition() {
        val layoutManager = recyclerView?.layoutManager as? LinearLayoutManager ?: return
        // TODO: Use adapter method when ColorAdabter is implemented
        // layoutManager.scrollToPositionWithOffset(
        //     (adapterRef as ColorAdabter).posSelect,
        //     (recyclerView!!.width / 2) - 50
        // )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        instance = null
        binding = null
        iColor = null
    }
}
