package hazem.nurmontage.videoquran.adabter

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import hazem.nurmontage.videoquran.GalleryPickerVideo
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.model.GallerySelected
import hazem.nurmontage.videoquran.model.PhotoItem
import hazem.nurmontage.videoquran.model.VideoItem
import hazem.nurmontage.videoquran.views.SquareImageView
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * Adapter for the video gallery picker grid.
 * Supports single-select and multi-select modes with folder filtering.
 * Converted from original Java GalleryVideoAdabters (191 lines).
 */
class GalleryVideoAdabters(
    private val APP_VERSION: String,
    resources: Resources,
    private val gallerySelectedList: List<GallerySelected>?,
    private val size: Int,
    private val iPicker: GalleryPickerVideo.IPicker?
) : RecyclerView.Adapter<GalleryVideoAdabters.MyViewHolder>() {

    private var videoItems: List<VideoItem>? = null
    private var allVideoItems: List<VideoItem>? = null
    private var videoItemSelect: VideoItem? = null
    private val bitmapPlaceHolder: BitmapDrawable

    init {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        bitmap.eraseColor(ViewCompat.MEASURED_STATE_MASK)
        bitmapPlaceHolder = BitmapDrawable(resources, bitmap)
    }

    fun doneItems(list: List<VideoItem>) {
        videoItems = list
        allVideoItems = ArrayList(list)
    }

    fun updateAll() {
        if (allVideoItems == null || videoItems == null) return
        (videoItems as? MutableList)?.clear()
        videoItems = ArrayList(allVideoItems!!)
        notifyDataSetChanged()
    }

    fun update(folder: String) {
        (videoItems as? MutableList)?.clear()
        allVideoItems?.let { all ->
            val filtered = all.filter { it.folderPath == folder }
            (videoItems as? MutableList)?.addAll(filtered)
        }
        notifyDataSetChanged()
    }

    fun setFolder(folder: String) {
        notifyDataSetChanged()
    }

    fun addItems(list: List<VideoItem>?) {
        videoItems = list
        if (iPicker != null) {
            if (list.isNullOrEmpty()) {
                iPicker.onEmptyList()
            }
        }
    }

    fun inselectItem(position: Int) {
        val list = videoItems ?: return
        if (position >= list.size) return
        val videoItem = list[position]
        videoItem.isSelect = false
        notifyItemChanged(position)
        updateNumbers(videoItem.number)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(R.layout.row_gallery, viewGroup, false)
        )
    }

    override fun onBindViewHolder(myViewHolder: MyViewHolder, i: Int) {
        val videoItem = videoItems?.get(i) ?: return
        myViewHolder.imageView.setNumber(videoItem.number)
        myViewHolder.imageView.onSelect(videoItem.isSelect)
        Glide.with(myViewHolder.itemView)
            .load(videoItem.path)
            .override(size, size)
            .centerCrop()
            .signature(ObjectKey(APP_VERSION))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .placeholder(bitmapPlaceHolder)
            .into(myViewHolder.imageView)
        myViewHolder.tvTime.text = videoItem.time
    }

    override fun getItemCount(): Int = videoItems?.size ?: 0

    fun clear() {
        (videoItems as? MutableList)?.clear()
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: SquareImageView = view.findViewById(R.id.img)
        val tvTime: TextCustumFont

        init {
            val tvTimeView: TextCustumFont = view.findViewById(R.id.tv_time)
            this.tvTime = tvTimeView
            tvTimeView.visibility = View.VISIBLE

            view.setOnClickListener {
                if (iPicker == null || adapterPosition < 0) return@setOnClickListener

                if (gallerySelectedList == null) {
                    // Single-select mode
                    val videoItem = videoItems?.get(adapterPosition) ?: return@setOnClickListener
                    if (videoItem === videoItemSelect) return@setOnClickListener
                    videoItemSelect?.let { prev ->
                        prev.isSelect = false
                        notifyItemChanged(prev.adabter_pos)
                    }
                    videoItemSelect = videoItem
                    videoItem.isSelect = true
                    imageView.onSelect(true)
                    videoItem.adabter_pos = adapterPosition
                    iPicker.onAdd(videoItem, adapterPosition)
                } else {
                    // Multi-select mode
                    val videoItem = videoItems?.get(adapterPosition) ?: return@setOnClickListener
                    videoItem.isSelect = !videoItem.isSelect
                    imageView.onSelect(videoItem.isSelect)
                    if (videoItem.isSelect) {
                        imageView.setNumber(gallerySelectedList.size + 1)
                        videoItem.number = imageView.getAnInt()
                        videoItem.adabter_pos = adapterPosition
                        iPicker.onAdd(videoItem, adapterPosition)
                    } else {
                        updateNumbers(imageView.getAnInt())
                        iPicker.onDelete(videoItem.gallerySelected ?: return@setOnClickListener)
                    }
                }
            }
        }
    }

    fun updateNumbers(startNumber: Int) {
        var i = startNumber
        while (i < (gallerySelectedList?.size ?: 0)) {
            val gallerySelected = gallerySelectedList!![i]
            val videoItem = gallerySelected.videoItem
            if (videoItem != null) {
                videoItem.number = videoItem.number - 1
                notifyItemChanged(videoItem.adabter_pos)
            }
            val photoItem = gallerySelected.photoItem
            if (photoItem != null) {
                photoItem.number = photoItem.number - 1
            }
            i++
        }
    }
}
