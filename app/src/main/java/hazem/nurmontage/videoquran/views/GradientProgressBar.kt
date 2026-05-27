package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

class GradientProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var progress: Int = 0
        set(value) {
            field = value.coerceIn(0, maxProgress)
            invalidate()
        }
    var maxProgress: Int = 100
        set(value) {
            field = value
            invalidate()
        }
    var trackColor: Int = Color.WHITE
        set(value) {
            field = value
            trackPaint.color = value
            invalidate()
        }
    var gradientColors: IntArray = intArrayOf(
        Color.parseColor("#a8ce46"), Color.parseColor("#D2DE49"), Color.parseColor("#F4D853")
    )
        set(value) {
            if (value.isEmpty()) return
            field = value
            createProgressShader()
            invalidate()
        }
    var cornerRadius: Float = 100f
        set(value) {
            field = value
            invalidate()
        }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = trackColor
        style = Paint.Style.FILL
    }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private var progressShader: LinearGradient? = null
    private val trackRect = RectF()
    private val progressRect = RectF()

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        createProgressShader()
    }

    private fun createProgressShader() {
        if (width <= 0 || height <= 0) return
        progressShader = LinearGradient(0f, 0f, width.toFloat(), 0f, gradientColors, null, Shader.TileMode.CLAMP)
        progressPaint.shader = progressShader
    }

    fun getMax(): Int = maxProgress

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        trackRect.set(0f, 0f, w, h)
        canvas.drawRoundRect(trackRect, cornerRadius, cornerRadius, trackPaint)

        if (maxProgress > 0) {
            progressRect.set(0f, 0f, w * (progress.toFloat() / maxProgress), h)
            canvas.drawRoundRect(progressRect, cornerRadius, cornerRadius, progressPaint)
        }
    }
}
