package hazem.nurmontage.videoquran.adabter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.Utils.DrawableHelper

/**
 * Adapter for selecting a Quran icon style (hafes, shamerli, nour_hode, amiri).
 * Each item displays an icon image with a selectable circular background.
 * Converted from original Java IconQuranAdabters (95 lines).
 */
class IconQuranAdabters(
    private val iconQuranCallback: IIconQuranCallback?,
    private val list: List<String>?,
    select: Int
) : RecyclerView.Adapter<IconQuranAdabters.ViewHolder>() {

    var select: Int = if (list != null && select >= list.size) 0 else select

    interface IIconQuranCallback {
        fun onIcon(name: String)
    }

    fun isHaveSelect(): Boolean = select != -1

    fun unselect() {
        if (select == -1) return
        val oldPos = select
        select = -1
        notifyItemChanged(oldPos)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(R.layout.row_anim, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val name = list?.get(i) ?: return
        viewHolder.animationItem.setImageResource(
            DrawableHelper.getIDDrawableIconByName(viewHolder.itemView.context, name)
        )
        if (i == select) {
            viewHolder.animationItem.setBackgroundResource(R.drawable.circle_item_menu_select)
        } else {
            viewHolder.animationItem.setBackgroundResource(R.drawable.circle_effect)
        }
    }

    override fun getItemCount(): Int = list?.size ?: 0

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val animationItem: ImageView = view.findViewById(R.id.anim_item)
        val disableView: ImageView = view.findViewById(R.id.iv_disable)

        init {
            view.setOnClickListener {
                if (iconQuranCallback == null || select == adapterPosition) return@setOnClickListener
                val oldPos = select
                select = adapterPosition
                notifyItemChanged(oldPos)
                notifyItemChanged(select)
                iconQuranCallback.onIcon(list?.get(adapterPosition) ?: return@setOnClickListener)
            }
        }
    }
}
