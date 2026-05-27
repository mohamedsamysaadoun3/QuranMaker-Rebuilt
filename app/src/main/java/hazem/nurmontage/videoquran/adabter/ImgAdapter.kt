package hazem.nurmontage.videoquran.adabter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import hazem.nurmontage.videoquran.R

/**
 * Simple image adapter for displaying a list of drawable resource IDs.
 * Used in the Pro subscription screen's auto-scrolling showcase.
 * Converted from original Java ImgAdapter (63 lines).
 */
class ImgAdapter(
    private val APP_VERSION: String,
    private val images: List<Int>?,
    private val size: Int
) : RecyclerView.Adapter<ImgAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.img)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(R.layout.row_img_bg, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val resId = images?.get(i) ?: return
        Glide.with(viewHolder.imageView)
            .load(resId)
            .override(size, size)
            .signature(ObjectKey(APP_VERSION))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .centerCrop()
            .into(viewHolder.imageView)
    }

    override fun getItemCount(): Int = images?.size ?: 0

    override fun getItemId(i: Int): Long = images?.get(i)?.toLong() ?: RecyclerView.NO_ID
}
