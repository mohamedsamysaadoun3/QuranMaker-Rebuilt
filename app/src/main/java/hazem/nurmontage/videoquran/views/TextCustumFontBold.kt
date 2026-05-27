package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * Custom TextView that applies the ReadexPro_Bold font.
 * Stub implementation – full drawing/attribute logic to be added later.
 */
class TextCustumFontBold @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var typeface: Typeface? = null

    init {
        init(context)
    }

    private fun init(context: Context) {
        if (typeface == null) {
            try {
                typeface = Typeface.createFromAsset(resources.assets, "fonts/ReadexPro_Bold.ttf")
                setTypeface(typeface)
            } catch (_: Exception) {
                // Font asset not found – stub mode, silently skip
            }
        }
    }
}
