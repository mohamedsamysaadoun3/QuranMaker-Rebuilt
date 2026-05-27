package hazem.nurmontage.videoquran.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class EyeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val eyeRect = RectF()
    private var background: Bitmap? = null
    private var eyeProgress = 0f

    fun setBackground(bmp: Bitmap?) {
        background = bmp
        invalidate()
    }

    fun setEyeProgress(progress: Float) {
        val bg = background ?: return
        eyeProgress = progress
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bg = background ?: return

        val drawPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val halfH = height / 2f
        var centerX = width / 2f
        val eyeW = width * 0.6f
        val eyeH = height * 0.6f * eyeProgress

        canvas.drawBitmap(bg, 0f, 0f, drawPaint)

        val eyePath = createEyePath(centerX, halfH, eyeW, eyeH)

        // Draw stretched columns
        var i = 0
        while (i <= 60) {
            val colX = (width * i) / 60f
            val halfEyeW = eyeW / 2f
            val dx = colX - centerX

            if (Math.abs(dx) > halfEyeW) {
                i++
                centerX = width / 2f
                continue
            }

            val curveHeight = (eyeH / 2f) * (1f - (dx * dx) / (halfEyeW * halfEyeW))
            val topY = halfH + curveHeight

            val nextI = i + 1
            val srcTop = Rect((bg.width * i) / 60, 0, (bg.width * nextI) / 60, bg.height / 2)
            val srcBottom = Rect((bg.width * i) / 60, bg.height / 2, (nextI * bg.width) / 60, bg.height)

            val dstTop = RectF(colX, 0f, width / 60f + colX, halfH - curveHeight)
            val dstBottom = RectF(colX, topY, width / 60f + colX, height.toFloat())

            canvas.drawBitmap(bg, srcTop, dstTop, drawPaint)
            canvas.drawBitmap(bg, srcBottom, dstBottom, drawPaint)

            i++
            centerX = width / 2f
        }

        // Clear eye shape
        val saveCount = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        drawPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.drawPath(eyePath, drawPaint)
        drawPaint.xfermode = null
        canvas.restoreToCount(saveCount)
    }

    private fun createEyePath(cx: Float, cy: Float, w: Float, h: Float): Path {
        val halfW = w / 2f
        val halfH = h / 2f
        return Path().apply {
            moveTo(cx - halfW, cy)
            quadTo(cx, cy - halfH, cx + halfW, cy)
            quadTo(cx, cy + halfH, cx - halfW, cy)
            close()
        }
    }

    fun openEye() {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 8500L
        animator.addUpdateListener { anim ->
            eyeProgress = anim.animatedValue as Float
            invalidate()
        }
        animator.repeatCount = 5
        animator.start()
    }
}
