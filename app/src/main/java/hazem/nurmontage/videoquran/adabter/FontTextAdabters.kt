package hazem.nurmontage.videoquran.adabter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.Utils.FontProvider
import hazem.nurmontage.videoquran.fragment.FontFragment
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * Adapter for displaying Quran font names in a selectable list.
 * Each item shows font name rendered in its own typeface and a number.
 * Converted from original Java FontTextAdabters (102 lines).
 */
class FontTextAdabters(
    private val fontProvider: FontProvider,
    private var iFontCallback: FontFragment.IFontCallback?,
    private var fontList: List<String>?,
    private var selected: Int
) : RecyclerView.Adapter<FontTextAdabters.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(R.layout.row_font, viewGroup, false)
        )
    }

    fun setSelected(position: Int) {
        try {
            val oldPos = selected
            selected = position
            notifyItemChanged(oldPos)
            notifyItemChanged(selected)
            fontList?.let { list ->
                val name = list[position]
                iFontCallback?.onAdd(fontProvider.getFullName(name) ?: "", fontProvider.getTypeface(name))
            }
        } catch (_: Exception) {
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val name = fontList?.get(i) ?: return
        viewHolder.nameFont.text = name
        viewHolder.tvNumber.text = (i + 1).toString()
        try {
            viewHolder.nameFont.typeface = fontProvider.getTypeface(name)
            if (selected == i) {
                viewHolder.nameFont.setTextColor(-14540254)
                viewHolder.nameFont.setBackgroundResource(R.drawable.btn_item_font_state)
            } else {
                viewHolder.nameFont.setTextColor(-1)
                viewHolder.nameFont.background = null
            }
        } catch (_: Exception) {
        }
    }

    override fun getItemCount(): Int = fontList?.size ?: 0

    fun clear() {
        iFontCallback = null
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameFont: TextCustumFont = view.findViewById(R.id.tv_font)
        val tvNumber: TextCustumFont = view.findViewById(R.id.tv_number)

        init {
            nameFont.setOnClickListener {
                if (iFontCallback == null || selected == adapterPosition) return@setOnClickListener
                val oldPos = selected
                selected = adapterPosition
                notifyItemChanged(oldPos)
                notifyItemChanged(selected)
                val name = fontList?.get(selected) ?: return@setOnClickListener
                iFontCallback!!.onAdd(fontProvider.getFullName(name) ?: "", fontProvider.getTypeface(name))
            }
        }
    }
}
