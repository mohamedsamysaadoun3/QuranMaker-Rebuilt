package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class TextCustumFontAR @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var typeface: Typeface? = null

    init {
        if (this.typeface == null) {
            val tf = Typeface.createFromAsset(resources.assets, "fonts/ReadexPro_Medium.ttf")
            this.typeface = tf
            setTypeface(tf)
        }
    }
}
