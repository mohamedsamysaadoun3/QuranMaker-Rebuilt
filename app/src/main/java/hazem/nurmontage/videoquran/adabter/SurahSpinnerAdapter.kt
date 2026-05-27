package hazem.nurmontage.videoquran.adabter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import hazem.nurmontage.videoquran.R

/**
 * Spinner adapter for surah name selection.
 * In Arabic mode shows the Arabic name (before " - "),
 * in English mode shows the English name (after " - ").
 * Converted from original Java SurahSpinnerAdapter (50 lines).
 */
class SurahSpinnerAdapter(
    context: Context,
    private val surahNames: Array<String>,
    private val isArabic: Boolean
) : ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, surahNames) {

    private val mContext: Context = context

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent, android.R.layout.simple_spinner_item)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent, android.R.layout.simple_spinner_item)
    }

    private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup, layoutRes: Int): View {
        val view = convertView ?: LayoutInflater.from(mContext).inflate(layoutRes, parent, false)
        Log.e("getCustomView", "" + position)
        val textView: TextView = view.findViewById(R.id.spinner_text)
        val parts = surahNames[position].split(" - ")
        textView.text = if (isArabic) {
            parts[0]
        } else {
            if (parts.size > 1) parts[1] else parts[0]
        }
        return view
    }
}
