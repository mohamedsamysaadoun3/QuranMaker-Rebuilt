package hazem.nurmontage.videoquran.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.databinding.FragmentProgressViewBinding
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * Progress view fragment that displays "current/total" during video rendering.
 * Converted from original Java ProgressViewFragment (47 lines).
 */
class ProgressViewFragment() : Fragment() {

    private var binding: FragmentProgressViewBinding? = null
    private var tvProgress: TextCustumFont? = null

    companion object {
        private var instance: ProgressViewFragment? = null

        @JvmStatic
        fun getInstance(): ProgressViewFragment {
            if (instance == null) {
                instance = ProgressViewFragment()
            }
            return instance!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflate = FragmentProgressViewBinding.inflate(inflater, container, false)
        binding = inflate
        val root = inflate.root
        tvProgress = root.findViewById(R.id.tv_progress)
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        instance = null
    }

    /**
     * Updates the progress text to "current/total".
     */
    fun update(current: Int, total: Int) {
        tvProgress?.text = "$current/$total"
    }
}
