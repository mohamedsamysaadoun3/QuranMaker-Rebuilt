package hazem.nurmontage.videoquran.fragment

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.Utils.Utils
import hazem.nurmontage.videoquran.adabter.ColorAdabter
import hazem.nurmontage.videoquran.common.Common
import hazem.nurmontage.videoquran.constant.AyaTextPreset
import hazem.nurmontage.videoquran.databinding.FragmentColorAyaBinding
import hazem.nurmontage.videoquran.model.BismilahEntity
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * Fragment for selecting the color and text preset of a Bismilah entity.
 * Converted from ColorBismilahFragment.java (162 lines).
 */
class ColorBismilahFragment() : Fragment() {

    companion object {
        var instance: ColorBismilahFragment? = null

        fun getInstance(
            callback: IBismilahEntityCallback,
            entity: BismilahEntity,
            resources: Resources
        ): ColorBismilahFragment {
            if (instance == null) {
                instance = ColorBismilahFragment(callback, entity, resources)
            }
            return instance!!
        }
    }

    // Callback interface matching EditBismilahEntityFragment.IBismilahEntityCallback
    interface IBismilahEntityCallback {
        fun updateAya(color: Int)
        fun updatePreset(preset: AyaTextPreset)
        fun onDone()
    }

    private var adapter: ColorAdabter? = null
    private var binding: FragmentColorAyaBinding? = null
    private var entitySelect: BismilahEntity? = null
    private var iEditSName: IBismilahEntityCallback? = null
    private var recyclerView: RecyclerView? = null
    private var resourcesRef: Resources? = null

    private var iColor: ColorAdabter.IColor? = object : ColorAdabter.IColor {
        override fun onColor(color: Int, position: Int) {
            if (iEditSName == null || entitySelect == null) return
            scrollToSelectedPosition()
            iEditSName?.updateAya(color)
        }
    }


    constructor(
        callback: IBismilahEntityCallback,
        entity: BismilahEntity,
        resources: Resources
    ) : this() {
        this.iEditSName = callback
        this.entitySelect = entity
        this.resourcesRef = resources
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflate = FragmentColorAyaBinding.inflate(inflater, container, false)
        binding = inflate
        val root = inflate.root as LinearLayout

        if (iEditSName != null && entitySelect != null && resourcesRef != null) {
            recyclerView = root.findViewById(R.id.rv_color)
            // TODO: ColorAdabter needs to be implemented
            adapter = ColorAdabter(
                iColor!!,
                Common.MUSLIM_AYA_COLORS,
                Utils.indexOf(Common.MUSLIM_AYA_COLORS, entitySelect!!.getClrAya())
            )
            recyclerView?.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                itemAnimator = null
                setHasFixedSize(true)
                this.adapter = this@ColorBismilahFragment.adapter
            }

            if (adapter!!.getPos_select() > 2) {
                scrollToSelectedPosition(adapter!!.getPos_select() - 2)
            }

            initTab(root)
            setupPresetButtons(root)

            root.findViewById<View>(R.id.btn_done).setOnClickListener {
                iEditSName?.onDone()
            }
        }

        return root
    }

    private fun setupPresetButtons(view: View) {
        val btnNone = view.findViewById<TextCustumFont>(R.id.btnNone)
        val btnOutline = view.findViewById<TextCustumFont>(R.id.btnOutline)
        val btnShadow = view.findViewById<TextCustumFont>(R.id.btnShadow)
        val btnGlow = view.findViewById<TextCustumFont>(R.id.btnGlow)

        btnNone.text = resourcesRef?.getString(R.string.preset_none)
        btnOutline.text = resourcesRef?.getString(R.string.preset_outline)
        btnShadow.text = resourcesRef?.getString(R.string.preset_shadow)
        btnGlow.text = resourcesRef?.getString(R.string.preset_glow)

        val textViewArr: Array<TextView> = arrayOf(btnNone, btnOutline, btnShadow, btnGlow)
        val presetArr: Array<AyaTextPreset> = arrayOf(
            AyaTextPreset.NONE, AyaTextPreset.OUTLINE, AyaTextPreset.SHADOW, AyaTextPreset.GLOW
        )

        for (i in 0 until 4) {
            textViewArr[i].setOnClickListener {
                selectPreset(textViewArr, i)
                iEditSName?.updatePreset(presetArr[i])
            }
        }

        val entity = entitySelect ?: return
        val currentPreset = entity.get(entity.getmPreset())
        val selectIndex = when (currentPreset) {
            AyaTextPreset.NONE -> 0
            AyaTextPreset.OUTLINE -> 1
            AyaTextPreset.SHADOW -> 2
            AyaTextPreset.GLOW -> 3
        }
        selectPreset(textViewArr, selectIndex)
    }

    private fun selectPreset(textViewArr: Array<TextView>, index: Int) {
        for (i in textViewArr.indices) {
            textViewArr[i].isSelected = i == index
        }
    }

    fun scrollToSelectedPosition() {
        val layoutManager = recyclerView?.layoutManager as? LinearLayoutManager ?: return
        layoutManager.scrollToPositionWithOffset(
            adapter!!.getPos_select(),
            recyclerView!!.width / 2 - 50
        )
    }

    fun scrollToSelectedPosition(position: Int) {
        val layoutManager = recyclerView?.layoutManager as? LinearLayoutManager ?: return
        layoutManager.scrollToPositionWithOffset(position, recyclerView!!.width / 2)
    }

    private fun initTab(view: View) {
        view.findViewById<View>(R.id.tab_layout).visibility = View.GONE
    }

    override fun onDestroyView() {
        binding = null
        instance = null
        iColor = null
        super.onDestroyView()
    }
}
