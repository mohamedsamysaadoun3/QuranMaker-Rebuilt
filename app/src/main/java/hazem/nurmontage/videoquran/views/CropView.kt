package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.ScaleGestureDetector
import android.view.View

/**
 * Image cropping View with pinch-to-zoom and drag support.
 * Stub implementation – full cropping/interaction logic to be added later.
 */
class CropView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var bitmap: Bitmap? = null
    private val cropPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    private val bitmapPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isFilterBitmap = true
    }
    private var cropRect: RectF = RectF()
    private var matrix: Matrix = Matrix()
    private var iCropCallback: ICropCallback? = null

    interface ICropCallback {
        fun onSizeChange()
    }

    fun setiCropCallback(callback: ICropCallback?) {
        this.iCropCallback = callback
    }

    var mDrawingX: Float = 0f
    var mDrawingY: Float = 0f

    fun getCropRect(): RectF = cropRect

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Stub: full crop drawing logic to be implemented
    }
}
