package hazem.nurmontage.videoquran.adabter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R

/**
 * Color picker adapter for background colors with rounded-rect color swatches.
 * Similar to ColorAdabter but uses cornerRadius=10 instead of oval shape.
 * Used for ipad background color selection.
 * Converted from original Java ColorBgAdabter (93 lines).
 */
class ColorBgAdabter(
    var iColorCallback: IColor?,
    private val colors: IntArray?,
    posSelect: Int
) : RecyclerView.Adapter<ColorBgAdabter.ViewHolder>() {

    private var enabled: Boolean = true
    var posSelect: Int = posSelect

    fun interface IColor {
        fun onColor(color: Int, position: Int)
    }

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image)

        init {
            view.setOnClickListener {
                if (iColorCallback == null || posSelect == adapterPosition || !enabled) return@setOnClickListener
                val oldPos = posSelect
                posSelect = adapterPosition
                notifyItemChanged(oldPos)
                notifyItemChanged(posSelect)
                iColorCallback!!.onColor(colors?.get(adapterPosition) ?: return@setOnClickListener, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(R.layout.row_color, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val color = colors?.get(i) ?: return
        setGradientBackground(viewHolder.imageView, viewHolder.itemView, color, i == posSelect)
    }

    fun setGradientBackground(view: View, view2: View, color: Int, isSelected: Boolean) {
        if (isSelected) {
            val gd = GradientDrawable()
            gd.shape = GradientDrawable.OVAL
            gd.cornerRadius = 10f
            gd.setStroke(3, -1)
            view2.background = gd
        } else {
            view2.background = null
        }
        val gd2 = GradientDrawable()
        gd2.shape = GradientDrawable.OVAL
        gd2.cornerRadius = 10f
        gd2.setColor(color)
        view.background = gd2
    }

    override fun getItemCount(): Int = colors?.size ?: 0

    fun getPos_select(): Int = posSelect
}
