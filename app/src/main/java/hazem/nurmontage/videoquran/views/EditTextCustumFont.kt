package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class EditTextCustumFont @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private var typeface: Typeface? = null

    init {
        if (typeface == null) {
            val tf = Typeface.createFromAsset(resources.assets, "fonts/arabic/خط الإبل.otf")
            typeface = tf
            setTypeface(tf)
        }
    }
}
