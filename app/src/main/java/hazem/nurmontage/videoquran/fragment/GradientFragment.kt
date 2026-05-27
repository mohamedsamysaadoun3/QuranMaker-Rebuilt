package hazem.nurmontage.videoquran.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.Utils.BillingPreferences
import hazem.nurmontage.videoquran.common.Common
import hazem.nurmontage.videoquran.databinding.FragmentColorsBinding
import hazem.nurmontage.videoquran.model.Gradient
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * Fragment displaying a horizontal gradient picker with angle adjustment seekbar.
 * Converted from original Java GradientFragment (123 lines).
 */
class GradientFragment() : Fragment() {

    // TODO: Replace with actual adapter when GradientAdabter is written
    // private var adapter: GradientAdabter? = null
    private var adapterRef: Any? = null
    private var binding: FragmentColorsBinding? = null
    private var gradient: Gradient? = null
    // TODO: Replace with actual callback type when GradientAdabter is written
    // private var iColor: GradientAdabter.IColor? = null
    private var iColor: Any? = null
    // TODO: Replace with EditIpadFragment.IIpadEditCallback when written
    private var iIpadEditCallback: Any? = null
    private var index: Int = 0
    private var recyclerView: RecyclerView? = null
    private var seekBarAngle: SeekBar? = null
    private var tvAngle: TextCustumFont? = null

    companion object {
        private var instance: GradientFragment? = null

        @JvmStatic
        fun getInstance(iIpadEditCallback: Any, index: Int): GradientFragment {
            if (instance == null) {
                instance = GradientFragment(iIpadEditCallback, index)
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

        // TODO: Uncomment when GradientAdabter is implemented
        // iColor = object : GradientAdabter.IColor {
        //     override fun onGradient(gradient: Gradient, position: Int) {
        //         if (this@GradientFragment.gradient == null) {
        //             binding?.root?.findViewById<View>(R.id.layout_edit_gradient)?.visibility = View.VISIBLE
        //         }
        //         this@GradientFragment.gradient = gradient
        //         gradient.angle = seekBarAngle?.progress ?: 81
        //         this@GradientFragment.index = position
        //         scrollToSelectedPosition()
        //         (iIpadEditCallback as? EditIpadFragment.IIpadEditCallback)?.onClick(gradient, position)
        //     }
        // }
        // val adapter = GradientAdabter(
        //     iColor as GradientAdabter.IColor,
        //     Common.getListGradientColor(),
        //     BillingPreferences.isSubscribed(requireContext()),
        //     index
        // )
        // adapterRef = adapter
        // recyclerView?.apply {
        //     layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        //     itemAnimator = null
        //     setHasFixedSize(true)
        //     this.adapter = adapter
        // }
        // gradient = adapter.select

        tvAngle = root.findViewById(R.id.tv_angle)
        seekBarAngle = root.findViewById(R.id.seekbar)

        if (gradient != null) {
            root.findViewById<View>(R.id.layout_edit_gradient).visibility = View.VISIBLE
            seekBarAngle?.progress = gradient!!.angle
        }

        tvAngle?.text = seekBarAngle?.progress.toString()

        seekBarAngle?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (gradient == null || iIpadEditCallback == null) return
                gradient!!.angle = progress
                tvAngle?.text = progress.toString()
                // TODO: Call iIpadEditCallback.onClick(gradient, index) when callback type is available
            }
        })

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
        // TODO: Use adapter method when GradientAdabter is implemented
        // layoutManager.scrollToPositionWithOffset(
        //     (adapterRef as GradientAdabter).posSelect,
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
