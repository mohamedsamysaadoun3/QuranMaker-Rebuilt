package hazem.nurmontage.videoquran.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.databinding.FragmentFontBinding

/**
 * Fragment for selecting a Quran icon style (hafes, shamerli, nour_hode, amiri).
 * Converted from original Java EditIconQuranFragment (107 lines).
 */
class EditIconQuranFragment() : Fragment() {

    private var fragmentBinding: FragmentFontBinding? = null
    private var iQuranIconCallback: IQuranIconCallback? = null
    private var icon: String? = null
    private var lastIcon: String? = null
    // TODO: Replace with actual adapter callback type when IconQuranAdabters is written
    // private var iconQuranCallback: IconQuranAdabters.IIconQuranCallback? = null

    interface IQuranIconCallback {
        fun add(icon: String)
        fun onCancel(lastIcon: String)
        fun onDone(icon: String)
    }

    companion object {
        private var instance: EditIconQuranFragment? = null

        @JvmStatic
        fun getInstance(callback: IQuranIconCallback, icon: String): EditIconQuranFragment {
            if (instance == null) {
                instance = EditIconQuranFragment(callback, icon)
            }
            return instance!!
        }
    }


    constructor(callback: IQuranIconCallback, icon: String) : this() {
        iQuranIconCallback = callback
        this.icon = icon
        lastIcon = icon
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
            val recyclerView = root.findViewById<RecyclerView>(R.id.rv)
            recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.itemAnimator = null
            recyclerView.setHasFixedSize(true)

            val iconList = arrayListOf("hafes", "shamerli", "nour_hode", "amiri")

            // TODO: Uncomment when IconQuranAdabters adapter is implemented
            // iconQuranCallback = object : IconQuranAdabters.IIconQuranCallback {
            //     override fun onIcon(iconName: String) {
            //         this@EditIconQuranFragment.icon = iconName
            //         iQuranIconCallback?.add(iconName)
            //     }
            // }
            // val adapter = IconQuranAdabters(iconQuranCallback!!, iconList, iconList.indexOf(icon))
            // if (adapter.select != -1) {
            //     icon = iconList[adapter.select]
            // }
            // recyclerView.adapter = adapter

            root.findViewById<View>(R.id.btn_done).setOnClickListener {
                iQuranIconCallback?.onDone(icon ?: "")
            }
            root.findViewById<View>(R.id.btn_cancel).setOnClickListener {
                iQuranIconCallback?.onCancel(lastIcon ?: "")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentBinding = null
        iQuranIconCallback = null
        instance = null
    }
}
