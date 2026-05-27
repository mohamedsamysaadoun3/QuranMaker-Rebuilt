package hazem.nurmontage.videoquran.adabter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.model.WordModel
import hazem.nurmontage.videoquran.views.TextCustumFont

class WordAyaAdabter(
    list: List<WordModel>?,
    private var iWordAya: IWordAya? = null
) : RecyclerView.Adapter<WordAyaAdabter.ViewHolder>() {

    interface IWordAya {
        fun onClick()
    }

    constructor(list: List<WordModel>) : this(list, null)

    var list: List<WordModel>? = list
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextCustumFont = view.findViewById(R.id.word_aya)

        init {
            text.setOnClickListener {
                val item = list?.get(adapterPosition) ?: return@setOnClickListener
                item.isSelected = !item.isSelected
                notifyItemChanged(adapterPosition)
                iWordAya?.onClick()
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(R.layout.row_word_aya, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val wordModel = list?.get(i) ?: return
        viewHolder.text.text = wordModel.w
        if (wordModel.isSelected) {
            viewHolder.text.setBackgroundResource(R.drawable.round_btn_quran_select)
            viewHolder.text.setTextColor(-12434878)
        } else {
            viewHolder.text.setBackgroundResource(R.drawable.round_btn_in_dark)
            viewHolder.text.setTextColor(-1)
        }
    }

    override fun getItemCount(): Int = list?.size ?: 0
}
