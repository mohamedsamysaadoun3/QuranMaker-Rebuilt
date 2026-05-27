package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * ImageView with blur effect, touch gestures, and entity management.
 * Stub implementation – full drawing/interaction logic to be added later.
 */
class BlurredImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), View.OnTouchListener {

    var iViewCallback: IViewCallback? = null

    interface IViewCallback {
        fun onDrawFinish()
        fun onEmtyClick()
        fun onEndMove()
        fun onEndScale()
        fun onSelect(entityView: Any?)
        fun onSquare()
        fun onWattermark()
    }

    var isRemoveWattermark: Boolean = false
    var bitmapNotBlur: Bitmap? = null
    var isVideo: Boolean = false
    var isDrawingSquareVideo: Boolean = false
    var isPlaying: Boolean = false
    var isPro: Boolean = false
    var bitmapOriginal: Bitmap? = null
    var isGlass: Boolean = false
    var progress: Float = 0f

    init {
        setOnTouchListener(this)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        // Stub: full touch handling to be implemented
        return false
    }
}
