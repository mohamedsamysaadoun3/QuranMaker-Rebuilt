package hazem.nurmontage.videoquran.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.Utils.FontProvider
import hazem.nurmontage.videoquran.databinding.FragmentFontBinding

/**
 * Fragment for selecting a Quran font with snap-scrolling RecyclerView.
 * Converted from original Java FontFragment (138 lines).
 */
class FontFragment() : Fragment() {

    private var fontSelect: String? = null
    private var fragmentBinding: FragmentFontBinding? = null
    private var iFontCallback: IFontCallback? = null
    private var isInit: Boolean = true
    private var lastTypeface: Typeface? = null
    private var lastFont: String? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var recyclerView: RecyclerView? = null
    private var typeface: Typeface? = null

    interface IFontCallback {
        fun onAdd(fontName: String, typeface: Typeface)
        fun onCancel(lastFont: String, lastTypeface: Typeface)
        fun onDone(fontName: String, typeface: Typeface)
    }

    companion object {
        private var instance: FontFragment? = null

        @JvmStatic
        fun getInstance(callback: IFontCallback, font: String, typeface: Typeface): FontFragment {
            if (instance == null) {
                instance = FontFragment(callback, font, typeface)
            }
            return instance!!
        }
    }


    constructor(callback: IFontCallback, font: String, typeface: Typeface) : this() {
        iFontCallback = callback
        lastFont = font
        lastTypeface = typeface
    }

    fun add(typeface: Typeface, fontName: String) {
        this.typeface = typeface
        fontSelect = fontName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflate = FragmentFontBinding.inflate(inflater, container, false)
        fragmentBinding = inflate
        val root = inflate.root

        try {
            val fontProvider = FontProvider(resources!!)
            recyclerView = root.findViewById(R.id.rv)

            val fontNameWithoutExt = lastFont?.let {
                if (it.length > 4) it.substring(0, it.length - 4) else it
            } ?: ""
            val indexOf = fontProvider.getFontNamesQuran().indexOf(fontNameWithoutExt)

            // TODO: Uncomment when FontTextAdabters adapter is implemented
            // val fontTextAdabters = FontTextAdabters(
            //     fontProvider,
            //     iFontCallback,
            //     fontProvider.getFontNamesQuran(),
            //     indexOf
            // )
            val llm = LinearLayoutManager(requireContext())
            linearLayoutManager = llm
            recyclerView?.layoutManager = llm
            recyclerView?.setHasFixedSize(true)
            // recyclerView?.adapter = fontTextAdabters

            val snapHelper = LinearSnapHelper()
            snapHelper.attachToRecyclerView(recyclerView)

            recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (isInit) {
                        isInit = false
                        return
                    }
                    val snapView = snapHelper.findSnapView(linearLayoutManager) ?: return
                    val position = linearLayoutManager?.getPosition(snapView) ?: return
                    this@FontFragment.recyclerView?.post {
                        // TODO: fontTextAdabters?.setSelected(position)
                    }
                }
            })

            if (indexOf > 1) {
                recyclerView?.scrollToPosition(indexOf - 1)
            } else if (indexOf >= 0) {
                recyclerView?.scrollToPosition(indexOf)
            }

            root.findViewById<View>(R.id.btn_done).setOnClickListener {
                iFontCallback?.onDone(fontSelect ?: "", typeface ?: Typeface.DEFAULT)
            }
            root.findViewById<View>(R.id.btn_cancel).setOnClickListener {
                if (iFontCallback != null && lastFont != null && lastTypeface != null) {
                    iFontCallback!!.onCancel(lastFont!!, lastTypeface!!)
                }
            }
        } catch (_: Exception) {
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentBinding = null
        iFontCallback = null
        instance = null
    }
}
