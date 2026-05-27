package hazem.nurmontage.videoquran.fragment

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.databinding.FragmentAddAudioBinding
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * Fragment offering three audio actions: upload, extract, cancel.
 * Converted from original Java AddAudioFragment (84 lines).
 */
class AddAudioFragment() : Fragment() {

    private var addAudioBinding: FragmentAddAudioBinding? = null
    private var iAudioCallback: IAudioCallback? = null
    private var resourcesRef: Resources? = null

    interface IAudioCallback {
        fun cancel()
        fun extract()
        fun upload()
    }

    companion object {
        private var instance: AddAudioFragment? = null

        @JvmStatic
        fun getInstance(callback: IAudioCallback, resources: Resources): AddAudioFragment {
            if (instance == null) {
                instance = AddAudioFragment(callback, resources)
            }
            return instance!!
        }
    }

    constructor(callback: IAudioCallback, resources: Resources) : this() {
        iAudioCallback = callback
        resourcesRef = resources
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflate = FragmentAddAudioBinding.inflate(inflater, container, false)
        addAudioBinding = inflate
        val root = inflate.root

        if (resourcesRef != null && iAudioCallback != null) {
            (root.findViewById<TextCustumFont>(R.id.tv_extract)).text =
                resourcesRef!!.getString(R.string.extract_audio)
            (root.findViewById<TextCustumFont>(R.id.tv_audio)).text =
                resourcesRef!!.getString(R.string.audio)

            root.findViewById<View>(R.id.btn_upload).setOnClickListener {
                iAudioCallback?.upload()
            }
            root.findViewById<View>(R.id.btn_extract).setOnClickListener {
                iAudioCallback?.extract()
            }
            root.findViewById<View>(R.id.btn_close).setOnClickListener {
                iAudioCallback?.cancel()
            }
        }
        return root
    }

    override fun onDestroyView() {
        addAudioBinding = null
        instance = null
        iAudioCallback = null
        super.onDestroyView()
    }
}
