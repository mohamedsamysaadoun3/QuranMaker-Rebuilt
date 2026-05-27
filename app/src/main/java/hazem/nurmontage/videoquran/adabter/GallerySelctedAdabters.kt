package hazem.nurmontage.videoquran.adabter

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.model.GallerySelected
import hazem.nurmontage.videoquran.views.SquareImageView
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * Adapter for the selected media strip at the bottom of the gallery picker.
 * Shows thumbnail + delete button for each selected photo/video.
 * Converted from original Java GallerySelctedAdabters (133 lines).
 */
class GallerySelctedAdabters(
    resources: Resources,
    private val iGallerySelected: IGallerySelected?,
    private val size: Int
) : RecyclerView.Adapter<GallerySelctedAdabters.MyViewHolder>() {

    private val gallerySelecteds: MutableList<GallerySelected> = ArrayList()
    private val bitmapPlaceHolder: BitmapDrawable

    interface IGallerySelected {
        fun inselectPhoto(index: Int)
        fun inselectVideo(index: Int)
    }

    init {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        bitmap.eraseColor(ViewCompat.MEASURED_STATE_MASK)
        bitmapPlaceHolder = BitmapDrawable(resources, bitmap)
    }

    fun getGallerySelecteds(): List<GallerySelected> = gallerySelecteds

    fun getSize(): Int = size

    fun addItemVideo(gallerySelected: GallerySelected) {
        gallerySelecteds.add(gallerySelected)
        gallerySelected.videoItem?.gallerySelected = gallerySelected
        notifyItemInserted(gallerySelecteds.size - 1)
    }

    fun addItemPhoto(gallerySelected: GallerySelected) {
        gallerySelecteds.add(gallerySelected)
        gallerySelected.photoItem?.gallerySelected = gallerySelected
        notifyItemInserted(gallerySelecteds.size - 1)
    }

    fun deletedItem(gallerySelected: GallerySelected) {
        val index = gallerySelecteds.indexOf(gallerySelected)
        if (index != -1) {
            gallerySelecteds.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun deletedItem(index: Int) {
        gallerySelecteds.removeAt(index)
        notifyItemRemoved(index)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(R.layout.row_gallery_select, viewGroup, false)
        )
    }

    override fun onBindViewHolder(myViewHolder: MyViewHolder, i: Int) {
        val item = gallerySelecteds[i]
        val path: String?
        if (item.videoItem != null) {
            path = item.videoItem!!.path
            myViewHolder.tvTime.visibility = View.VISIBLE
            myViewHolder.tvTime.text = item.videoItem!!.time
        } else {
            path = item.photoItem?.path
            myViewHolder.tvTime.visibility = View.GONE
        }
        Glide.with(myViewHolder.itemView)
            .load(path)
            .override(size, size)
            .centerCrop()
            .placeholder(bitmapPlaceHolder)
            .into(myViewHolder.imageView)
    }

    override fun getItemCount(): Int = gallerySelecteds.size

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: SquareImageView = view.findViewById(R.id.img)
        val tvTime: TextCustumFont = view.findViewById(R.id.tv_time)
        private val btnDeleted: ImageButton = view.findViewById(R.id.btn_deleted)

        init {
            btnDeleted.visibility = View.VISIBLE
            tvTime.visibility = View.VISIBLE
            btnDeleted.setOnClickListener {
                val item = gallerySelecteds[adapterPosition]
                deletedItem(adapterPosition)
                if (item.videoItem != null) {
                    iGallerySelected?.inselectVideo(item.index)
                } else {
                    iGallerySelected?.inselectPhoto(item.index)
                }
            }
        }
    }
}
