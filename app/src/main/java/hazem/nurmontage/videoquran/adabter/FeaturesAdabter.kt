package hazem.nurmontage.videoquran.adabter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.model.ModelFeatures
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * Adapter for displaying feature list items (e.g., Pro subscription features).
 * Converted from original Java FeaturesAdabter (53 lines).
 */
class FeaturesAdabter(
    private var list: List<ModelFeatures>?
) : RecyclerView.Adapter<FeaturesAdabter.ViewHolder>() {

    private var isSubscribe: Boolean = false

    fun setSubscribe(subscribe: Boolean) {
        isSubscribe = subscribe
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextCustumFont = view.findViewById(R.id.tv_feature)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(R.layout.row_feature, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        list?.get(i)?.let { viewHolder.text.text = it.name }
    }

    override fun getItemCount(): Int = list?.size ?: 0
}
