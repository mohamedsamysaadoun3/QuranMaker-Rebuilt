package hazem.nurmontage.videoquran.adabter

import android.content.Context
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import hazem.nurmontage.videoquran.R

/**
 * Adapter for the About screen – displays a list of about items (text + optional image).
 * Converted from original Java AboutAdabters (147 lines).
 */
class AboutAdabters(
    private val mContext: Context,
    private val APP_VERSION: String,
    private val mModelAboutList: List<ModelAbout>?,
    private val mDimensionW: Int,
    private val mDimensionH: Int
) : RecyclerView.Adapter<AboutAdabters.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.tv)
        val imageView1: ImageView = view.findViewById(R.id.img)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(R.layout.row_billing, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val modelAbout = mModelAboutList?.get(i) ?: return
        viewHolder.textView.gravity = modelAbout.gravity
        if (modelAbout.sizeText == 19) {
            viewHolder.textView.paint.isFakeBoldText = true
        } else {
            viewHolder.textView.paint.isFakeBoldText = false
        }
        viewHolder.textView.setTextSize(2, modelAbout.sizeText.toFloat())
        viewHolder.textView.text = HtmlCompat.fromHtml(modelAbout.text, 0)
        if (modelAbout.image1 != -1) {
            viewHolder.imageView1.visibility = View.VISIBLE
            Glide.with(mContext)
                .asBitmap()
                .load(modelAbout.image1)
                .override(mDimensionW, mDimensionH)
                .centerInside()
                .signature(ObjectKey(APP_VERSION))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(viewHolder.imageView1)
        } else {
            viewHolder.imageView1.visibility = View.GONE
            Glide.with(mContext).clear(viewHolder.imageView1)
        }
    }

    override fun getItemCount(): Int = mModelAboutList?.size ?: 0

    /**
     * Model class for about screen items.
     * Contains text (with gravity), size, and optional image resource IDs.
     */
    class ModelAbout {
        var image1: Int = -1
            private set
        var image2: Int = -1
            private set
        var sizeText: Int = 16
            private set
        var text: String = ""
            private set
        var gravity: Int = 0
            private set

        constructor(pair: Pair<String, Int>, image1: Int) {
            this.image2 = -1
            this.sizeText = 16
            this.text = pair.first
            this.gravity = pair.second
            this.image1 = image1
        }

        constructor(sizeText: Int, pair: Pair<String, Int>, image1: Int) {
            this.image2 = -1
            this.text = pair.first
            this.gravity = pair.second
            this.image1 = image1
            this.sizeText = sizeText
        }

        constructor(image1: Int, image2: Int, pair: Pair<String, Int>) {
            this.sizeText = 16
            this.text = pair.first
            this.gravity = pair.second
            this.image1 = image1
            this.image2 = image2
        }

        constructor(pair: Pair<String, Int>) {
            this.image1 = -1
            this.image2 = -1
            this.sizeText = 16
            this.text = pair.first
            this.gravity = pair.second
        }

        constructor(pair: Pair<String, Int>, image1: Int, sizeText: Int) {
            this.image2 = -1
            this.text = pair.first
            this.gravity = pair.second
            this.sizeText = sizeText
            this.image1 = image1
        }

        constructor(sizeText: Int, pair: Pair<String, Int>) {
            this.image1 = -1
            this.image2 = -1
            this.text = pair.first
            this.gravity = pair.second
            this.sizeText = sizeText
        }
    }
}
