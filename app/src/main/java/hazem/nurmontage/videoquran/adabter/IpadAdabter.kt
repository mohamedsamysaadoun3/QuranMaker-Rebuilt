package hazem.nurmontage.videoquran.adabter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.fragment.EditIpadFragment
import hazem.nurmontage.videoquran.model.IpadItem

/**
 * Adapter for selecting iPad frame styles (IPAD, CLASSIC, CASSET, RECT, etc.).
 * Items with "many options" (positions 0, 1, 7, 8, 9) show glass/blur toggle dots.
 * Non-subscribers see a PRO badge on items beyond position 1.
 * Converted from original Java IpadAdabter (135 lines).
 */
class IpadAdabter(
    private val isSubscribe: Boolean,
    posSelect: Int,
    private var ipadSelected: Int,
    val ipadEditCallback: EditIpadFragment.IIpadEditCallback?,
    private val ipadItems: List<IpadItem>?,
    private var isGlass: Boolean
) : RecyclerView.Adapter<IpadAdabter.ViewHolder>() {

    private var posSelect: Int = posSelect

    private fun isManyOption(position: Int): Boolean {
        return position == 0 || position == 1 || position == 7 || position == 8 || position == 9
    }

    fun getPos_select(): Int = posSelect

    private fun updateDote(view: View, view2: View) {
        if (isGlass) {
            view.alpha = 1.0f
            view2.alpha = 0.5f
        } else {
            view2.alpha = 1.0f
            view.alpha = 0.5f
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPro: ImageView = view.findViewById(R.id.iv_pro)
        val lytOption: LinearLayout = view.findViewById(R.id.view_option)
        val imageView: ImageView = view.findViewById(R.id.img)
        val vDot1: View = view.findViewById(R.id.dot1)
        val vDot2: View = view.findViewById(R.id.dot2)

        init {
            view.setOnClickListener {
                if (posSelect == adapterPosition) {
                    if (!isManyOption(adapterPosition)) return@setOnClickListener
                    isGlass = !isGlass
                    ipadEditCallback?.onGlassType(isGlass)
                }
                if (!isSubscribe && adapterPosition > 1) {
                    ipadEditCallback?.onDialogPremium()
                    return@setOnClickListener
                }
                if (ipadEditCallback != null) {
                    val ipadItem = ipadItems?.get(adapterPosition) ?: return@setOnClickListener
                    notifyItemChanged(posSelect)
                    posSelect = adapterPosition
                    ipadSelected = ipadItem.ipadType.ordinal
                    notifyItemChanged(posSelect)
                    ipadEditCallback.onChangeType(ipadSelected)
                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(R.layout.row_ipad, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val ipadItem = ipadItems?.get(i) ?: return
        Glide.with(viewHolder.imageView)
            .asBitmap()
            .load(ipadItem.getImg())
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(viewHolder.imageView)

        if (isManyOption(i)) {
            viewHolder.lytOption.visibility = View.VISIBLE
            updateDote(viewHolder.vDot1, viewHolder.vDot2)
        } else {
            viewHolder.lytOption.visibility = View.GONE
        }

        if (ipadItem.ipadType.ordinal == ipadSelected) {
            viewHolder.itemView.alpha = 1.0f
            viewHolder.imageView.setBackgroundResource(R.drawable.ipad_selected)
            posSelect = i
        } else {
            viewHolder.itemView.alpha = 0.4f
            viewHolder.imageView.setBackgroundResource(R.drawable.watch_btn_outline)
        }

        if (!isSubscribe) {
            if (i > 1) {
                viewHolder.ivPro.visibility = View.VISIBLE
            } else {
                viewHolder.ivPro.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = ipadItems?.size ?: 0
}
