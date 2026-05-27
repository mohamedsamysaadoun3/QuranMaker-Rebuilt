package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class TextCustumFontBold @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var typeface: Typeface? = null

    init {
        if (typeface == null) {
            val tf = Typeface.createFromAsset(resources.assets, "fonts/ReadexPro_Bold.ttf")
            typeface = tf
            setTypeface(tf)
        }
    }
}
