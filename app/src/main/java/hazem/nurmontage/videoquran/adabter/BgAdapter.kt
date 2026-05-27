package hazem.nurmontage.videoquran.adabter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.signature.ObjectKey
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.fragment.ChangeBgFragment
import hazem.nurmontage.videoquran.model.BgItem
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

/**
 * Adapter for displaying background items with selection state.
 * Supports partial-bind (payload "alpha") for smooth selection transitions.
 * Uses stable IDs and alpha-based selected/unselected states.
 * Converted from original Java BgAdapter (140 lines).
 */
class BgAdapter(
    private val APP_VERSION: String,
    private val iBgCallback: ChangeBgFragment.IChangeBgCallback?,
    private val images: List<BgItem>,
    private val size: Int,
    initialSelected: Int
) : RecyclerView.Adapter<BgAdapter.ViewHolder>() {

    var selected: Int = initialSelected
        private set

    /** Public alias used by ChangeBgFragment.scrollToSelected() */
    val selectedPosition: Int get() = selected

    init {
        setHasStableIds(true)
    }

    fun add(bgItem: BgItem) {
        val size = images.size
        (images as? MutableList)?.add(bgItem)
        notifyItemInserted(size)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.img)

        init {
            view.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                if (pos == selected) {
                    // Re-click on selected item – just notify callback
                    iBgCallback?.onAdd(images[pos])
                    return@setOnClickListener
                }

                val oldSelected = selected
                selected = pos
                if (oldSelected != -1) {
                    notifyItemChanged(oldSelected, "alpha")
                }
                notifyItemChanged(selected, "alpha")
                iBgCallback?.onAdd(images[pos])
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(R.layout.row_img_bg, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        // Full bind – load image + apply state
        loadAndBind(viewHolder, i)
        applyState(viewHolder, i)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            // Partial bind – only update selection state (no image reload)
            applyState(viewHolder, i)
        } else {
            // No payload → full bind
            loadAndBind(viewHolder, i)
            applyState(viewHolder, i)
        }
    }

    private fun loadAndBind(viewHolder: ViewHolder, i: Int) {
        Glide.with(viewHolder.imageView)
            .load(images[i].id)
            .override(size, size)
            .signature(ObjectKey(APP_VERSION))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .transform(
                MultiTransformation(
                    CenterCrop(),
                    RoundedCornersTransformation(10, 8)
                )
            )
            .into(viewHolder.imageView)
    }

    override fun getItemCount(): Int = images.size

    override fun getItemId(i: Int): Long = images[i].id.toLong()

    private fun applyState(viewHolder: ViewHolder, i: Int) {
        val isSelected = i == selected
        val alpha = if (isSelected) 1.0f else 0.65f
        if (isSelected) {
            viewHolder.itemView.setBackgroundResource(R.drawable.ipad_selected)
        } else {
            viewHolder.itemView.setBackgroundColor(0)
        }
        viewHolder.imageView.alpha = alpha
    }
}
