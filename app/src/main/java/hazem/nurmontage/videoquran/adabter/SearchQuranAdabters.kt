package hazem.nurmontage.videoquran.adabter

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.model.ItemQuranSearch

/**
 * Adapter for Quran search results with text highlighting and range selection.
 * Supports multi-click range selection (minSelected..maxSelected).
 * Matching text within ayah is highlighted with a gold color (-10929).
 * Converted from original Java SearchQuranAdabters (131 lines).
 */
class SearchQuranAdabters(
    private val callback: ISearchQuranCallback?
) : RecyclerView.Adapter<SearchQuranAdabters.ViewHolder>() {

    private val searchList: MutableList<ItemQuranSearch> = ArrayList()
    private var minSelected: Int = -1
    private var maxSelected: Int = -1

    interface ISearchQuranCallback {
        fun onClick(minSelected: Int, maxSelected: Int, itemQuranSearch: ItemQuranSearch)
    }

    fun getSize(): Int = searchList.size

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(R.layout.row_search_quran, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val item = searchList[i]
        if (item.aya.isNotEmpty()) {
            viewHolder.name.text = "${item.surahName} (${item.to})"
            if (item.startSpannable != -1) {
                val spannableString = SpannableString(item.aya)
                spannableString.setSpan(
                    ForegroundColorSpan(-10929),
                    item.startSpannable,
                    item.endSpannble,
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                viewHolder.aya.text = spannableString
            } else {
                viewHolder.aya.text = item.aya
            }
        } else {
            viewHolder.name.text = item.surahIndex.toString()
        }

        viewHolder.itemView.setBackgroundColor(
            if (minSelected != -1 && i >= minSelected && i <= maxSelected) -14540254 else 0
        )
    }

    override fun getItemCount(): Int = searchList.size

    fun setList(list: List<ItemQuranSearch>) {
        searchList.clear()
        searchList.addAll(list)
        notifyDataSetChanged()
    }

    fun add(itemQuranSearch: ItemQuranSearch) {
        searchList.add(itemQuranSearch)
        notifyItemInserted(searchList.size - 1)
    }

    fun clear() {
        val size = searchList.size
        if (size == 0) return
        searchList.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun getMinSelected(): Int = minSelected

    fun getMaxSelected(): Int = maxSelected

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tv_surah_name_and_number)
        val aya: TextView = view.findViewById(R.id.tv_surah)

        init {
            view.setOnClickListener {
                val bindingAdapterPosition = bindingAdapterPosition
                if (bindingAdapterPosition == -1) return@setOnClickListener

                if (minSelected == -1) {
                    minSelected = bindingAdapterPosition
                    maxSelected = bindingAdapterPosition
                } else if (bindingAdapterPosition < minSelected) {
                    minSelected = bindingAdapterPosition
                } else if (bindingAdapterPosition > maxSelected) {
                    maxSelected = bindingAdapterPosition
                } else {
                    minSelected = bindingAdapterPosition
                    maxSelected = bindingAdapterPosition
                }

                notifyDataSetChanged()
                if (callback != null) {
                    callback.onClick(minSelected, maxSelected, searchList[bindingAdapterPosition])
                }
            }
        }
    }
}
