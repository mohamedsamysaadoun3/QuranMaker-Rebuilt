package hazem.nurmontage.videoquran.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.databinding.FragmentProgressViewBinding

/**
 * Simplified progress view fragment that hides the progress indicators.
 * Used during pre-render stages where only the progress bar itself is shown.
 * Converted from original Java SimpleProgressViewFragment (41 lines).
 */
class SimpleProgressViewFragment() : Fragment() {

    private var fragmentBinding: FragmentProgressViewBinding? = null

    companion object {
        private var instance: SimpleProgressViewFragment? = null

        @JvmStatic
        fun getInstance(): SimpleProgressViewFragment {
            if (instance == null) {
                instance = SimpleProgressViewFragment()
            }
            return instance!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentProgressViewBinding.inflate(inflater, container, false)
        fragmentBinding = binding
        val root = binding.root
        root.setBackgroundColor(0)
        root.findViewById<View>(R.id.view_1).visibility = View.GONE
        root.findViewById<View>(R.id.view_2).visibility = View.GONE
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentBinding = null
        instance = null
    }
}
