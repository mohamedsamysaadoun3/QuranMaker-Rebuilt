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
import hazem.nurmontage.videoquran.views.SquareImageView

/**
 * Adapter for the photo gallery picker grid.
 * Supports single-select and multi-select modes.
 * In single-select: gallerySelectedList is null, one photo at a time.
 * In multi-select: gallerySelectedList tracks selected items with numbering.
 * Converted from original Java GalleryPickerAdabters (178 lines).
 */
class GalleryPickerAdabters(
    private val APP_VERSION: String,
    resources: Resources,
    private val gallerySelectedList: List<GallerySelected>?,
    private val size: Int,
    private val iPicker: GalleryPickerVideo.IPicker?
) : RecyclerView.Adapter<GalleryPickerAdabters.MyViewHolder>() {

    private var paths: List<PhotoItem>? = null
    private var allPaths: List<PhotoItem>? = null
    private var photoItemSelected: PhotoItem? = null
    private val bitmapPlaceHolder: BitmapDrawable

    init {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        bitmap.eraseColor(ViewCompat.MEASURED_STATE_MASK)
        bitmapPlaceHolder = BitmapDrawable(resources, bitmap)
    }

    fun addItems(list: List<PhotoItem>?) {
        paths = list
        if (iPicker != null) {
            if (list.isNullOrEmpty()) {
                iPicker.onEmptyList()
            }
        }
    }

    fun doneItems(list: List<PhotoItem>) {
        paths = list
        allPaths = ArrayList(list)
    }

    fun updateAll() {
        if (allPaths == null || paths == null) return
        (paths as? MutableList)?.clear()
        paths = ArrayList(allPaths!!)
        notifyDataSetChanged()
    }

    fun update(folder: String) {
        (paths as? MutableList)?.clear()
        allPaths?.let { all ->
            val filtered = all.filter { it.folder == folder }
            (paths as? MutableList)?.addAll(filtered)
        }
        notifyDataSetChanged()
    }

    fun inselectItem(position: Int) {
        val list = paths ?: return
        if (position >= list.size) return
        val photoItem = list[position]
        photoItem.isSelect = false
        notifyItemChanged(position)
        updateNumbers(photoItem.number)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(R.layout.row_gallery, viewGroup, false)
        )
    }

    override fun onBindViewHolder(myViewHolder: MyViewHolder, i: Int) {
        val photoItem = paths?.get(i) ?: return
        myViewHolder.imageView.setNumber(photoItem.number)
        myViewHolder.imageView.onSelect(photoItem.isSelect)
        Glide.with(myViewHolder.itemView)
            .load(photoItem.path)
            .override(size, size)
            .centerCrop()
            .signature(ObjectKey(APP_VERSION))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .placeholder(bitmapPlaceHolder)
            .into(myViewHolder.imageView)
    }

    override fun getItemCount(): Int = paths?.size ?: 0

    fun clear() {
        (paths as? MutableList)?.clear()
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: SquareImageView = view.findViewById(R.id.img)

        init {
            view.setOnClickListener {
                if (iPicker == null || adapterPosition < 0) return@setOnClickListener

                if (gallerySelectedList == null) {
                    // Single-select mode
                    val photoItem = paths?.get(adapterPosition) ?: return@setOnClickListener
                    if (photoItem === photoItemSelected) return@setOnClickListener
                    photoItemSelected?.let { prev ->
                        prev.isSelect = false
                        notifyItemChanged(prev.adabter_pos)
                    }
                    photoItem.isSelect = true
                    imageView.onSelect(true)
                    photoItemSelected = photoItem
                    photoItem.adabter_pos = adapterPosition
                    iPicker.onAdd(photoItem, adapterPosition)
                } else {
                    // Multi-select mode
                    val photoItem = paths?.get(adapterPosition) ?: return@setOnClickListener
                    photoItem.isSelect = !photoItem.isSelect
                    imageView.onSelect(photoItem.isSelect)
                    if (photoItem.isSelect) {
                        imageView.setNumber(gallerySelectedList.size + 1)
                        photoItem.number = imageView.getAnInt()
                        photoItem.adabter_pos = adapterPosition
                        iPicker.onAdd(photoItem, adapterPosition)
                    } else {
                        updateNumbers(imageView.getAnInt())
                        iPicker.onDelete(photoItem.gallerySelected ?: return@setOnClickListener)
                    }
                }
            }
        }
    }

    fun updateNumbers(startNumber: Int) {
        if (gallerySelectedList == null) return
        var i = startNumber
        while (i < gallerySelectedList.size) {
            val photoItem = gallerySelectedList[i].photoItem
            if (photoItem != null) {
                photoItem.number = photoItem.number - 1
                notifyItemChanged(photoItem.adabter_pos)
            }
            i++
        }
    }
}
