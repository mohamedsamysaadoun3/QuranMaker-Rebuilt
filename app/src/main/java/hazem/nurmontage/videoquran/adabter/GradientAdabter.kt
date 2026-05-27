package hazem.nurmontage.videoquran.adabter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.model.Gradient

/**
 * Adapter for selecting a gradient background with 3-color gradient swatches.
 * First 2 gradients are free; remaining require subscription.
 * Converted from original Java GradientAdabter (119 lines).
 */
class GradientAdabter(
    var iColorCallback: IColor?,
    private val colors: List<Gradient>?,
    private val isSubscribe: Boolean,
    posSelect: Int
) : RecyclerView.Adapter<GradientAdabter.ViewHolder>() {

    private val maxFree: Int = 1
    private var posSelect: Int = posSelect

    interface IColor {
        fun onGradient(gradient: Gradient, position: Int)
    }

    fun getSelect(): Gradient? {
        return if (posSelect >= 0 && colors != null && posSelect < colors.size) {
            colors[posSelect]
        } else null
    }

    fun setGradientBackground(view: View, view2: View, gradient: Gradient, isSelected: Boolean) {
        if (isSelected) {
            val gd = GradientDrawable()
            gd.shape = GradientDrawable.OVAL
            gd.cornerRadius = 100f
            gd.setStroke(3, -1)
            view2.background = gd
        } else {
            view2.background = null
        }
        val gd2 = GradientDrawable()
        gd2.shape = GradientDrawable.OVAL
        gd2.cornerRadius = 100f
        gd2.colors = intArrayOf(gradient.getColor(), gradient.getSecond(), gradient.getThree())
        view.background = gd2
    }

    fun setGradientBackground(view: View, color: Int) {
        val gd = GradientDrawable()
        gd.setColor(color)
        gd.shape = GradientDrawable.OVAL
        gd.cornerRadius = 100f
        view.background = gd
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image)
        val imageLayer: ImageView = view.findViewById(R.id.image)

        init {
            setGradientBackground(imageView, -1895825408)
            view.setOnClickListener {
                if (iColorCallback != null) {
                    if ((isSubscribe || adapterPosition <= 1) && posSelect != adapterPosition) {
                        val oldPos = posSelect
                        posSelect = adapterPosition
                        notifyItemChanged(oldPos)
                        notifyItemChanged(posSelect)
                        iColorCallback!!.onGradient(colors?.get(adapterPosition) ?: return@setOnClickListener, adapterPosition)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(R.layout.row_color, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val gradient = colors?.get(i) ?: return
        setGradientBackground(viewHolder.imageView, viewHolder.itemView, gradient, i == posSelect)
        if (!isSubscribe && i > 1) {
            viewHolder.imageLayer.visibility = View.VISIBLE
        } else {
            viewHolder.imageLayer.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = colors?.size ?: 0

    fun getPos_select(): Int = posSelect
}
