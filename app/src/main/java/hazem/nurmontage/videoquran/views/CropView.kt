package hazem.nurmontage.videoquran.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import hazem.nurmontage.videoquran.Utils.UtilsBitmap
import hazem.nurmontage.videoquran.common.Common

class CropView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface ICropCallback {
        fun onSizeChange()
    }

    var iCropCallback: ICropCallback? = null

    private var bitmap: Bitmap? = null
    private val cropPaint = Paint().apply {
        color = -15605 // yellow
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }
    private val bitmapPaint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
    }
    private var cropRect = RectF()
    private var matrix = Matrix()
    private var scale = 0f
    private var scaleFactor = 1f
    private var mDrawingX = 0f
    private var mDrawingY = 0f
    private var mWidth = 0f
    private var mHeight = 0f
    private var mCanvasWidth = 0f
    private var mCanvasHeight = 0f
    private var minW = 0f
    private var minH = 0f
    private var radius = 0
    private var hintAnimationPlayed = false
    private var hintAnimator: ValueAnimator? = null
    private var initialHintRectWidth = 0f
    private var initialHintRectHeight = 0f
    private var initialHintRectCenterX = 0f
    private var initialHintRectCenterY = 0f
    private var isDragging = false
    private var startX = 0f
    private var startY = 0f
    private var lastFocusX = 0f
    private var lastFocusY = 0f
    private val touchTolerance = 10

    private val scaleGestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            lastFocusX = detector.focusX
            lastFocusY = detector.focusY
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var sf = detector.scaleFactor
            if (sf.isNaN() || sf.isInfinite()) return false
            scaleFactor *= sf
            var w = cropRect.width() * sf
            var h = cropRect.height() * sf
            if (w < minW) w = minW
            if (h < minH) h = minH
            if (w > mWidth) w = cropRect.width()
            if (h > mHeight) h = cropRect.height()
            val dx = detector.focusX - lastFocusX
            val dy = detector.focusY - lastFocusY
            val cx = cropRect.centerX()
            val cy = cropRect.centerY()
            val hw = w / 2f
            val hh = h / 2f
            cropRect.set(cx - hw, cy - hh, cx + hw, cy + hh)
            moveCropRect(dx, dy)
            lastFocusX = detector.focusX
            lastFocusY = detector.focusY
            invalidate()
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {}
    })

    fun setMDrawingX(x: Float) { mDrawingX = x }
    fun setMDrawingY(y: Float) { mDrawingY = y }
    fun getMDrawingX(): Float = mDrawingX
    fun getMDrawingY(): Float = mDrawingY

    fun setBitmap(bmp: Bitmap, rect: android.graphics.RectF, cornerRadius: Int, skipAnim: Boolean) {
        bitmap = bmp
        radius = cornerRadius
        mCanvasWidth = (width - paddingStart - paddingEnd).toFloat()
        mCanvasHeight = (height - paddingTop - paddingBottom).toFloat()
        val bw = bmp.width.toFloat()
        val bh = bmp.height.toFloat()
        scale = minOf(mCanvasWidth / bw, mCanvasHeight / bh)
        mWidth = bw * scale
        mHeight = bh * scale
        mDrawingX = (mCanvasWidth - mWidth) * 0.5f
        mDrawingY = (mCanvasHeight - mHeight) * 0.5f
        matrix = Matrix()
        matrix.postScale(scale, scale)
        cropRect = RectF(rect.left * scale, rect.top * scale, rect.right * scale, rect.bottom * scale)
        minH = Common.MIN_SQUARE_H * scale
        minW = Common.MIN_SQUARE_W * scale
        invalidate()
        if (skipAnim || width <= 0 || height <= 0) return
        initialHintRectWidth = cropRect.width()
        initialHintRectHeight = cropRect.height()
        initialHintRectCenterX = cropRect.centerX()
        initialHintRectCenterY = cropRect.centerY()
        startHintAnimation()
    }

    fun setBitmapLast(bmp: Bitmap, rect: Rect, cornerRadius: Int, skipAnim: Boolean) {
        bitmap = bmp
        cropRect = RectF(rect.left.toFloat(), rect.top.toFloat(), rect.right.toFloat(), rect.bottom.toFloat())
        radius = cornerRadius
        mCanvasWidth = (width - paddingStart - paddingEnd).toFloat()
        mCanvasHeight = (height - paddingTop - paddingBottom).toFloat()
        mDrawingY = (mCanvasHeight - bmp.height) * 0.5f
        mWidth = mCanvasWidth
        mHeight = bmp.height.toFloat()
        val m = Matrix()
        m.postScale(mCanvasWidth / mWidth, mCanvasWidth / mWidth)
        m.postTranslate(0f, mDrawingY)
        invalidate()
        if (skipAnim || width <= 0 || height <= 0) return
        initialHintRectWidth = cropRect.width()
        initialHintRectHeight = cropRect.height()
        initialHintRectCenterX = cropRect.centerX()
        initialHintRectCenterY = cropRect.centerY()
        startHintAnimation()
    }

    private fun startHintAnimation() {
        hintAnimator?.let { if (it.isRunning) it.cancel() }
        hintAnimationPlayed = true
        hintAnimator = ValueAnimator.ofFloat(1f, 1.8f).apply {
            duration = 700L
            interpolator = AccelerateDecelerateInterpolator()
            repeatCount = 3
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener { anim ->
                val value = anim.animatedValue as Float
                val w = initialHintRectWidth * value
                val h = initialHintRectHeight * value
                val hw = w / 2f
                val hh = h / 2f
                cropRect.set(
                    initialHintRectCenterX - hw,
                    initialHintRectCenterY - hh,
                    initialHintRectCenterX + hw,
                    initialHintRectCenterY + hh
                )
                invalidate()
            }
            start()
        }
    }

    fun getRectSquare(): Rect = Rect(
        Math.round(cropRect.left / scale),
        Math.round(cropRect.top / scale),
        Math.round(cropRect.right / scale),
        Math.round(cropRect.bottom / scale)
    )

    fun getmY(): Float = if (bitmap == null) 0.4f else maxOf(cropRect.top / mHeight, 0f)
    fun getmX(): Float = if (bitmap == null) 0.4f else maxOf(cropRect.left / mWidth, 0f)
    fun getmW(): Float = if (bitmap == null) 1f else cropRect.width() / mWidth
    fun getmH(): Float = if (bitmap == null) 1f else cropRect.height() / mHeight
    fun getCropRect(): RectF = cropRect

    fun getCroppedBitmap(): Bitmap? {
        val bmp = bitmap ?: return null
        var left = Math.round(cropRect.left / scale)
        var top = Math.round(cropRect.top / scale)
        if (left < 0) left = 0
        if (top < 0) top = 0
        val right = Math.min(Math.round(cropRect.right / scale), bmp.width)
        val bottom = Math.min(Math.round(cropRect.bottom / scale), bmp.height)
        return UtilsBitmap.cropToSquareWithRoundCornersPlusScale(bmp, Rect(left, top, right, bottom), radius, Common.MIN_SQUARE_W, Common.MIN_SQUARE_H)
    }

    private fun moveCropRect(dx: Float, dy: Float) {
        var left = cropRect.left + dx
        var top = cropRect.top + dy
        var right = cropRect.right + dx
        var bottom = cropRect.bottom + dy

        if (left < 0f) { right = cropRect.width(); left = 0f }
        if (top < 0f) { bottom = cropRect.height(); top = 0f }
        if (right > mWidth) { left = mWidth - cropRect.width(); right = mWidth }
        if (bottom > mHeight) { top = mHeight - cropRect.height(); bottom = mHeight }

        var w = right - left
        if (w < minW) {
            if (dx > 0f) right = left + minW else left = right - minW
            w = right - left
        }
        var h = bottom - top
        if (h < minH) {
            if (dy > 0f) bottom = top + minH else top = bottom - minH
        }
        cropRect.set(left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bmp = bitmap ?: return
        canvas.save()
        canvas.translate(mDrawingX, mDrawingY)
        canvas.clipRect(0, 0, bmp.width, bmp.height)
        canvas.drawBitmap(bmp, matrix, bitmapPaint)
        canvas.drawRoundRect(cropRect, radius.toFloat(), radius.toFloat(), cropPaint)
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        hintAnimator?.let { if (it.isRunning) it.cancel() }
        hintAnimationPlayed = true

        val scaleHandled = scaleGestureDetector.onTouchEvent(event)
        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (!scaleGestureDetector.isInProgress) {
                    isDragging = true
                    startX = x
                    startY = y
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!scaleGestureDetector.isInProgress && isDragging && event.pointerCount == 1) {
                    val dx = x - startX
                    val dy = y - startY
                    moveCropRect(dx, dy)
                    startX = x
                    startY = y
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (event.actionIndex == 0) isDragging = false
            }
            MotionEvent.ACTION_POINTER_UP -> {
                isDragging = false
            }
        }

        if (scaleHandled) { invalidate(); return true }
        if (isDragging || event.action == MotionEvent.ACTION_DOWN) { invalidate(); return true }
        return super.onTouchEvent(event)
    }

    fun croppedBitmap(): android.graphics.Bitmap? {
        return getCroppedBitmap()
    }
    
    fun rectSquare(): android.graphics.RectF {
        return android.graphics.RectF(cropRect)
    }
}
