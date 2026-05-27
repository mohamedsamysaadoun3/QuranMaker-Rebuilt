package hazem.nurmontage.videoquran.adabter

import android.graphics.Bitmap
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
 * Adapter for displaying background items in a list (lighter version).
 * Uses Glide with CenterCrop + RoundedCornersTransformation.
 * Converted from original Java BgAdabterL (91 lines).
 */
class BgAdabterL(
    private val APP_VERSION: String,
    val iBgCallback: ChangeBgFragment.IChangeBgCallback?,
    private var images: List<BgItem>?,
    private val size: Int
) : RecyclerView.Adapter<BgAdabterL.ViewHolder>() {

    private var selected: Int = 0

    fun interface IBgCallback {
        fun onBgClick(position: Int)
    }

    fun add(bgItem: BgItem) {
        try {
            (images as? MutableList)?.add(bgItem)
            notifyItemInserted(images?.size ?: 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getPos_select(): Int = selected

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.img)

        init {
            view.setOnClickListener {
                if (iBgCallback != null) {
                    selected = adapterPosition
                    iBgCallback.onAdd(images?.get(adapterPosition) ?: return@setOnClickListener)
                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(R.layout.row_img_bg, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val item = images?.get(i) ?: return
        Glide.with(viewHolder.imageView)
            .asBitmap()
            .load(item.id)
            .override(size, size)
            .signature(ObjectKey(APP_VERSION))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .transform(
                MultiTransformation(
                    CenterCrop(),
                    RoundedCornersTransformation(8, 0, RoundedCornersTransformation.CornerType.ALL)
                )
            )
            .into(viewHolder.imageView)
    }

    override fun getItemCount(): Int = images?.size ?: 0
}
