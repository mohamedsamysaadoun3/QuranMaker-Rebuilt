package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton

class ButtonCustumFontBilling @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.buttonStyle
) : AppCompatButton(context, attrs, defStyleAttr) {

    private var typeface: Typeface? = null

    init {
        if (typeface == null) {
            val tf = Typeface.createFromAsset(resources.assets, "fonts/ReadexPro_Medium.ttf")
            typeface = tf
            setTypeface(tf)
        }
    }
}
