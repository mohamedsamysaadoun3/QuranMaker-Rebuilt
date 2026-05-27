package hazem.nurmontage.videoquran.adabter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.model.Template

/**
 * Adapter for the user's saved projects on the WorkUser screen.
 * Shows video thumbnail with frame microsecond, name, date, and popup menu button.
 * Converted from original Java WorkUserAdabter (115 lines).
 */
class WorkUserAdabter(
    private val APP_VERSION: String,
    private var images: List<Template>,
    val iWorkUserCallback: IWorkUserCallback?,
    private val w: Int,
    private val h: Int
) : RecyclerView.Adapter<WorkUserAdabter.ViewHolder>() {

    interface IWorkUserCallback {
        fun onClick(template: Template)
        fun toMenu(template: Template, view: View, position: Int)
    }

    fun remove(position: Int) {
        try {
            if (position < images.size) {
                (images as? MutableList)?.removeAt(position)
            }
            notifyItemRemoved(position)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun add(position: Int, template: Template) {
        try {
            if (position < images.size) {
                (images as? MutableList)?.add(position, template)
            } else {
                (images as? MutableList)?.add(template)
            }
            notifyItemInserted(position)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.iv_thumbnail)
        private val btnMenu: ImageButton = view.findViewById(R.id.btn_menu)
        val ivRatio: ImageView = view.findViewById(R.id.iv_ratio)
        private val tvName: TextView = view.findViewById(R.id.tv_name)
        private val tvDate: TextView = view.findViewById(R.id.tv_date)

        fun bindName(name: String?) { tvName.text = name }
        fun bindDate(date: String?) { tvDate.text = date }

        init {
            btnMenu.setOnClickListener {
                if (iWorkUserCallback != null) {
                    iWorkUserCallback.toMenu(images[adapterPosition], it, adapterPosition)
                }
            }
            view.setOnClickListener {
                if (iWorkUserCallback != null) {
                    iWorkUserCallback.onClick(images[adapterPosition])
                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(R.layout.row_work_user, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val template = images[i]
        if (template.fileInfo != null) {
            viewHolder.bindName(template.fileInfo!!.formattedDate)
            viewHolder.bindDate(template.fileInfo!!.timedDate)
        }
        Glide.with(viewHolder.imageView)
            .asBitmap()
            .load(template.uri_video)
            .frame(1000000L)
            .centerInside()
            .override(w, h)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .signature(ObjectKey(APP_VERSION))
            .placeholder(R.drawable.broken_image_24px)
            .into(viewHolder.imageView)
    }

    override fun getItemCount(): Int = images.size
}
