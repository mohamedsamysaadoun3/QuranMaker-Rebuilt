package hazem.nurmontage.videoquran.adabter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.fragment.audio_effect.Reverbe
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * Adapter for selecting reverb presets (Normal, Masjid, Studio, etc.).
 * Supports toggle-on/toggle-off behavior – clicking selected item deselects it.
 * Converted from original Java ReverbeAdabter (91 lines).
 */
class ReverbeAdabter(
    private var list: List<Reverbe>?,
    private val iReverbCallback: IReverbPresetCallback?,
    private var select: Int
) : RecyclerView.Adapter<ReverbeAdabter.ViewHolder>() {

    interface IReverbPresetCallback {
        fun cmd(cmdFfmpeg: String?, position: Int)
        fun pause()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextCustumFont = view.findViewById(R.id.word_aya)
        val ivBtnPlay: ImageView = view.findViewById(R.id.iv_btn_play)

        init {
            view.setOnClickListener {
                if (iReverbCallback != null) {
                    iReverbCallback.pause()
                    if (select == adapterPosition) {
                        // Toggle off – deselect
                        val oldPos = select
                        select = -1
                        notifyItemChanged(oldPos)
                        notifyItemChanged(adapterPosition)
                        return@setOnClickListener
                    }
                    val oldPos = select
                    select = adapterPosition
                    notifyItemChanged(oldPos)
                    notifyItemChanged(select)
                    iReverbCallback.cmd(list?.get(adapterPosition)?.cmdFfmpeg, adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(R.layout.row_reverbe, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        list?.get(i)?.let { reverbe ->
            viewHolder.text.text = reverbe.name
        }
        if (select == i) {
            viewHolder.itemView.setBackgroundResource(R.drawable.item_reverb_select)
            viewHolder.ivBtnPlay.setImageResource(R.drawable.pause_24px)
        } else {
            viewHolder.itemView.setBackgroundResource(R.drawable.round_btn_in_dark)
            viewHolder.ivBtnPlay.setImageResource(R.drawable.play_arrow_24px)
        }
    }

    fun getList(): List<Reverbe>? = list

    override fun getItemCount(): Int = list?.size ?: 0
}
