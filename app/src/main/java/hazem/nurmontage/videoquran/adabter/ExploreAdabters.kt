package hazem.nurmontage.videoquran.adabter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.model.ExploreItem
import hazem.nurmontage.videoquran.views.SquareImageViewSimple
import hazem.nurmontage.videoquran.views.TextCustumFont
import java.io.File

/**
 * Adapter for the gallery folder explorer – displays folder thumbnails with name/size.
 * Converted from original Java ExploreAdabters (81 lines).
 */
class ExploreAdabters(
    private val exploreItems: List<ExploreItem>?,
    private val size: Int,
    private val iExplore: IExplore?,
    private val folderSelect: String
) : RecyclerView.Adapter<ExploreAdabters.MyViewHolder>() {

    interface IExplore {
        fun done()
        fun folder(file: File?, name: String, path: String)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(R.layout.row_explore, viewGroup, false)
        )
    }

    override fun onBindViewHolder(myViewHolder: MyViewHolder, i: Int) {
        val item = exploreItems?.get(i) ?: return
        Glide.with(myViewHolder.itemView)
            .load(item.firstFilePath)
            .override(size, size)
            .centerCrop()
            .placeholder(R.drawable.image_24px)
            .into(myViewHolder.imageView)
        myViewHolder.tvName.text = item.name
        myViewHolder.tvSize.text = item.size
    }

    override fun getItemCount(): Int = exploreItems?.size ?: 0

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: SquareImageViewSimple = view.findViewById(R.id.img)
        val tvName: TextCustumFont = view.findViewById(R.id.tv_name)
        val tvSize: TextCustumFont = view.findViewById(R.id.tv_size)

        init {
            view.setOnClickListener {
                if (iExplore != null) {
                    val item = exploreItems?.get(adapterPosition) ?: return@setOnClickListener
                    iExplore.folder(item.folder, item.name, item.path)
                }
            }
        }
    }
}
