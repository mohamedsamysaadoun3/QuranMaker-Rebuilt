package hazem.nurmontage.videoquran.adabter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.entity_timeline.EntityBismilahTimeline
import hazem.nurmontage.videoquran.fragment.EffectBismilahFragment

/**
 * Adapter for selecting transition effects on Bismilah entities.
 * Supports "in" and "out" transition types with icon rotation.
 * Converted from original Java TransitionBismilahAdabters (129 lines).
 */
class TransitionBismilahAdabters(
    private val iTransition: EffectBismilahFragment.ITransition?,
    list: List<TransitionItem>,
    select: Int,
    private val entityQuranTimeline: EntityBismilahTimeline
) : RecyclerView.Adapter<TransitionBismilahAdabters.ViewHolder>() {

    private var list: List<TransitionItem> = list
    private var max: Int = list.size
    var select: Int = select
    private var type: String = "in"

    fun update(list: List<TransitionItem>, type: String, select: Int) {
        this.select = select
        this.list = list
        this.type = type
        this.max = list.size
        notifyDataSetChanged()
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
        val item = list[i]
        viewHolder.animationItem.rotation = item.angle.toFloat()
        viewHolder.animationItem.setImageResource(item.idRessource)
        if (i == select) {
            viewHolder.animationItem.setBackgroundResource(R.drawable.circle_item_menu_select)
        } else {
            viewHolder.animationItem.setBackgroundResource(R.drawable.circle_effect)
        }
    }

    override fun getItemCount(): Int = max

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val animationItem: ImageView = view.findViewById(R.id.anim_item)
        val disableView: ImageView = view.findViewById(R.id.iv_disable)

        init {
            view.setOnClickListener {
                if (iTransition == null || select == adapterPosition) return@setOnClickListener
                val oldPos = select
                select = adapterPosition
                notifyItemChanged(oldPos)
                notifyItemChanged(select)
                val item = list[adapterPosition]
                if (type == "in") {
                    iTransition.`in`(item.type, entityQuranTimeline)
                } else if (type == "out") {
                    iTransition.out(item.type, entityQuranTimeline)
                }
            }
        }
    }

    /**
     * Data class for transition items with type name, icon resource, and rotation angle.
     */
    data class TransitionItem(
        val type: String,
        val idRessource: Int,
        val angle: Int
    )
}
