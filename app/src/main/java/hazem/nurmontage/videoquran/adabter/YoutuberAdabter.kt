package hazem.nurmontage.videoquran.adabter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.model.YoutuberModel
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

/**
 * Adapter for displaying YouTuber showcase thumbnails with rounded corners.
 * Clicking an item opens the YouTuber's link.
 * Converted from original Java YoutuberAdabter (73 lines).
 */
class YoutuberAdabter(
    private val iYoutuber: IYoutuber?,
    private val images: List<YoutuberModel>?,
    private val APP_VERSION: String,
    private val w: Int,
    private val h: Int
) : RecyclerView.Adapter<YoutuberAdabter.ViewHolder>() {

    interface IYoutuber {
        fun onClick(link: String)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.img)

        init {
            view.findViewById<View>(R.id.thumbnail_ytb).visibility = View.VISIBLE
            view.setOnClickListener {
                if (iYoutuber != null) {
                    val item = images?.get(adapterPosition) ?: return@setOnClickListener
                    iYoutuber.onClick(item.lnk)
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
            .load(item.img)
            .override(w, h)
            .signature(ObjectKey(APP_VERSION))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .transform(
                MultiTransformation(
                    RoundedCornersTransformation(8, 0, RoundedCornersTransformation.CornerType.ALL)
                )
            )
            .into(viewHolder.imageView)
    }

    override fun getItemCount(): Int = images?.size ?: 0
}
