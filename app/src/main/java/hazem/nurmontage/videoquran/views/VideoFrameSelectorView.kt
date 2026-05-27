package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Video frame selector for timeline – displays thumbnails and cursor.
 * Stub implementation – full frame loading/interaction logic to be added later.
 */
class VideoFrameSelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val framePaint: Paint = Paint()
    private val cursorPaint: Paint = Paint().apply {
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }
    private val frameRect: RectF = RectF()
    private var frameCount: Int = 7
    private var selectedFrameIndex: Int = 0
    private var cursorX: Float = 0f
    private var frameWidth: Float = 0f
    private var frameHeight: Float = 0f
    private var frameSpacing: Float = 1f
    private var cornerRadius: Float = 10f
    private var videoUri: Uri? = null

    var onFrameSelectedListener: OnFrameSelectedListener? = null

    interface OnFrameSelectedListener {
        fun onFrameSelected(index: Int, bitmap: Bitmap?)
    }

    class BitmapFrame(val bitmap: Bitmap, val time: Long)

    fun setVideoUri(uri: Uri?) {
        videoUri = uri
        // Stub: frame loading to be implemented
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            val x = event.x.coerceIn(0f, width.toFloat())
            cursorX = x
            selectedFrameIndex = (x / (frameWidth + frameSpacing)).toInt()
                .coerceIn(0, frameCount - 1)
            invalidate()
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Stub: full frame drawing logic to be implemented
    }
}
