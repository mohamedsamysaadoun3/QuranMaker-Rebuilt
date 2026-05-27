package hazem.nurmontage.videoquran.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class EyeOpenView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    private val eyePath = Path()
    private val eyeRect = RectF()
    private var bitmap: Bitmap? = null
    private var centerX = 0f
    private var centerY = 0f
    private var radiusX = 0f
    private var radiusYFull = 0f
    private var currentRY = 0f
    private var wrapOffset = 50f

    fun setBitmap(bmp: Bitmap?) {
        bitmap = bmp
        invalidate()
    }

    fun startEyeOpenAnimation(duration: Long) {
        val animator = ValueAnimator.ofFloat(0f, radiusYFull)
        animator.duration = duration
        animator.addUpdateListener { anim ->
            currentRY = anim.animatedValue as Float
            invalidate()
        }
        animator.repeatCount = 5
        animator.start()
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        centerX = w / 2f
        centerY = h / 2f
        radiusX = w * 0.4f
        radiusYFull = h * 0.2f
        super.onSizeChanged(w, h, oldW, oldH)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bmp = bitmap ?: return

        canvas.drawBitmap(bmp, 0f, 0f, paint)

        eyePath.reset()
        eyeRect.set(centerX - radiusX, centerY - currentRY, centerX + radiusX, centerY + currentRY)
        eyePath.addOval(eyeRect, Path.Direction.CW)

        canvas.saveLayer(null, null)
        canvas.drawPath(eyePath, clearPaint)
        canvas.restore()

        // Top part
        val mapTop = map(currentRY, 0f, radiusYFull, 1f, 0f)
        canvas.save()
        canvas.clipRect(0f, 0f, width.toFloat(), centerY - currentRY)
        canvas.scale(1f, mapTop, centerX, centerY - currentRY)
        canvas.drawBitmap(bmp, 0f, 0f, paint)
        canvas.restore()

        // Bottom part
        val mapBottom = map(currentRY, 0f, radiusYFull, 1f, 0f)
        canvas.save()
        canvas.clipRect(0f, centerY + currentRY, width.toFloat(), height.toFloat())
        canvas.scale(1f, mapBottom, centerX, centerY + currentRY)
        canvas.drawBitmap(bmp, 0f, 0f, paint)
        canvas.restore()
    }

    private fun map(value: Float, inMin: Float, inMax: Float, outMin: Float, outMax: Float): Float {
        return outMin + (value - inMin) / (inMax - inMin) * (outMax - outMin)
    }
}
