package hazem.nurmontage.videoquran.adabter

import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.constant.ResizeType
import hazem.nurmontage.videoquran.model.ItemDimension
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * Adapter for selecting video output dimensions (TikTok, YouTube, Instagram, etc.).
 * Each item shows an icon, name, and resolution text.
 * Converted from original Java DimensionAdabters (137 lines).
 */
class DimensionAdabters(
    private var mDimensionList: List<ItemDimension>?,
    private var mIDimensionCallback: IDimensionCallback?,
    private var listDim: List<Pair<Int, Int>>,
    selected: Int = 0
) : RecyclerView.Adapter<DimensionAdabters.ViewHolder>() {

    private var selected: Int = selected

    interface IDimensionCallback {
        fun done()
        fun isCustomSize(isCustom: Boolean, resizeType: ResizeType)
        fun onCustumSize(w: Int, h: Int, resizeType: Int, id: String, image: Int)
    }

    fun setSelected(selected: Int) {
        this.selected = selected
    }

    fun getSelected(): Int = selected

    fun get(): Int = mDimensionList?.get(getSelected())?.resizeType?.ordinal ?: 0

    fun update(list: List<ItemDimension>) {
        mDimensionList?.let { (it as? MutableList)?.clear() }
        mDimensionList = list
    }

    fun clear() {
        mDimensionList?.let { (it as? MutableList)?.clear() }
        mDimensionList = null
        mIDimensionCallback = null
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(R.layout.row_aspect, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val dim = listDim[i]
        viewHolder.layout.layoutParams.width = dim.first
        viewHolder.layout.layoutParams.height = dim.second
        val split = mDimensionList?.get(i)?.name?.split("\n") ?: return
        viewHolder.name.text = split[0]
        if (split.size > 1) {
            viewHolder.dimension.text = split[1]
        }
        Glide.with(viewHolder.itemView)
            .asBitmap()
            .centerInside()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .load(mDimensionList!![i].image)
            .into(viewHolder.imageView)
        if (i == selected) {
            viewHolder.layout.setBackgroundResource(R.drawable.rect_btn_select)
        } else {
            viewHolder.layout.setBackgroundResource(R.drawable.rect_btn)
        }
    }

    override fun getItemCount(): Int = mDimensionList?.size ?: 0

    fun getResizeSelected(): ResizeType {
        return mDimensionList?.get(getSelected())?.resizeType ?: ResizeType.SQUARE
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout: FrameLayout = view.findViewById(R.id.layout)
        val imageView: ImageView = view.findViewById(R.id.icon)
        val name: TextCustumFont = view.findViewById(R.id.aspect_name)
        val dimension: TextCustumFont = view.findViewById(R.id.aspect_size)

        init {
            view.setOnClickListener {
                if (mIDimensionCallback != null) {
                    val oldPos = selected
                    selected = adapterPosition
                    notifyItemChanged(oldPos)
                    notifyItemChanged(selected)
                    val item = mDimensionList?.get(adapterPosition) ?: return@setOnClickListener
                    mIDimensionCallback!!.onCustumSize(
                        item.w, item.h,
                        item.resizeType.ordinal,
                        item.id, item.image
                    )
                }
            }
        }
    }
}
