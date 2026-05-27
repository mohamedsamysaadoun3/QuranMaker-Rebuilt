package hazem.nurmontage.videoquran.Utils

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager

/**
 * Disables vertical scroll on RecyclerView.
 * Used for horizontal-only scrolling lists.
 */
class NonScrollableLinearLayoutManager(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    orientation: Int = VERTICAL
) : LinearLayoutManager(context, attrs, defStyleAttr, orientation) {

    override fun canScrollVertically(): Boolean = false
}
