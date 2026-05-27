package hazem.nurmontage.videoquran.fragment

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.databinding.FragmentEditSNameBinding
import hazem.nurmontage.videoquran.model.SurahNameEntity
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * Fragment for editing Surah name properties: font, color, and edit text.
 * Converted from original Java EditS_NameFragment (95 lines).
 */
class EditS_NameFragment() : Fragment() {

    private var entitySelect: SurahNameEntity? = null
    private var fragmentBinding: FragmentEditSNameBinding? = null
    private var iEditSName: IEditS_Name? = null
    private var resourcesRef: Resources? = null

    interface IEditS_Name {
        fun onColor(entity: SurahNameEntity)
        fun onDone()
        fun onEdit(entity: SurahNameEntity)
        fun onFont(entity: SurahNameEntity)
        fun update()
    }

    companion object {
        private var instance: EditS_NameFragment? = null

        @JvmStatic
        fun getInstance(callback: IEditS_Name, resources: Resources, entity: SurahNameEntity): EditS_NameFragment {
            if (instance == null) {
                instance = EditS_NameFragment(callback, resources, entity)
            }
            return instance!!
        }
    }

    constructor(callback: IEditS_Name, resources: Resources, entity: SurahNameEntity) : this() {
        iEditSName = callback
        resourcesRef = resources
        entitySelect = entity
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflate = FragmentEditSNameBinding.inflate(inflater, container, false)
        fragmentBinding = inflate
        val root = inflate.root

        if (iEditSName != null && resourcesRef != null && entitySelect != null) {
            (root.findViewById<TextCustumFont>(R.id.tv_color)).text =
                resourcesRef!!.getString(R.string.color)
            (root.findViewById<TextCustumFont>(R.id.tv_edit)).text =
                resourcesRef!!.getString(R.string.edit)
            (root.findViewById<TextCustumFont>(R.id.tv_font)).text =
                resourcesRef!!.getString(R.string.font)

            root.findViewById<View>(R.id.btn_font).setOnClickListener {
                iEditSName?.onFont(entitySelect!!)
            }
            root.findViewById<View>(R.id.btn_color).setOnClickListener {
                iEditSName?.onColor(entitySelect!!)
            }
            root.findViewById<View>(R.id.btn_edit).setOnClickListener {
                iEditSName?.onEdit(entitySelect!!)
            }
        }
        return root
    }

    override fun onDestroyView() {
        instance = null
        iEditSName = null
        fragmentBinding = null
        super.onDestroyView()
    }
}
