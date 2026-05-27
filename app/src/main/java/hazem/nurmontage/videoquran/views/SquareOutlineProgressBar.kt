package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import hazem.nurmontage.videoquran.Utils.LocaleHelper
import java.util.Locale

class SquareOutlineProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private val path = Path()
    private val partialPath = Path()
    private val pathMeasure = PathMeasure()

    var progress: Int = 0
        set(value) {
            val clamped = maxOf(0, minOf(value, maxProgress))
            if (field != clamped) {
                field = clamped
                invalidate()
            }
        }

    var maxProgress: Int = 100
        set(value) {
            field = maxOf(1, value)
            invalidate()
        }

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var trackColor: Int = 587202559 // 0x230000FF
    private var gradientColors: IntArray = intArrayOf(
        Color.parseColor("#a8ce46"),
        Color.parseColor("#D2DE49"),
        Color.parseColor("#F4D853")
    )
    private var progressShader: LinearGradient? = null
    private var cornerRadius = 0f
    private var strokeWidth = 0f
    private var strHint: String
    private var xH = 0f
    private var xP = 0f
    private var yProgress = 0f
    private var yHint = 0f

    init {
        trackPaint.style = Paint.Style.STROKE
        trackPaint.strokeCap = Paint.Cap.ROUND
        trackPaint.strokeJoin = Paint.Join.ROUND
        trackPaint.color = trackColor

        progressPaint.style = Paint.Style.STROKE
        progressPaint.strokeCap = Paint.Cap.ROUND
        progressPaint.strokeJoin = Paint.Join.ROUND

        textPaint.color = -1 // White
        val tf = Typeface.createFromAsset(resources.assets, "fonts/ReadexPro_Medium.ttf")
        textPaint.typeface = tf
        strHint = if (LocaleHelper.getLanguage(context) == "ar") {
            "يرجى عدم قفل الشاشة أو التبديل إلى تطبيقات أخرى."
        } else {
            "Please don't lock the screen or switch to other apps."
        }
    }

    fun getMax(): Int = maxProgress

    fun setCornerRadius(radius: Float) {
        cornerRadius = radius
        invalidate()
    }

    fun setStrokeWidth(width: Float) {
        strokeWidth = width
        trackPaint.strokeWidth = width
        progressPaint.strokeWidth = width
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension((size / 1.618034f).toInt(), size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        updateShader(w, h)

        val wf = w.toFloat()
        textPaint.textSize = 0.033f * wf
        val textBounds = Rect()
        cornerRadius = 0.04f * wf
        strokeWidth = wf * 0.0085f
        trackPaint.strokeWidth = strokeWidth
        progressPaint.strokeWidth = strokeWidth

        textPaint.getTextBounds(strHint, 0, strHint.length, textBounds)
        val halfStroke = strokeWidth / 2f
        rect.set(halfStroke, halfStroke, width - halfStroke, height - halfStroke)
        yProgress = rect.centerY() - textBounds.height()
        yHint = rect.centerY() + textBounds.height()
        xH = rect.centerX() - textBounds.width() * 0.5f

        textPaint.getTextBounds("100", 0, 3, textBounds)
        xP = rect.centerX() - textBounds.width() * 0.5f
    }

    private fun updateShader(w: Int, h: Int) {
        if (w == 0 || h == 0) return
        val shader = LinearGradient(
            0f, 0f, w.toFloat(), h.toFloat(),
            gradientColors, null, Shader.TileMode.CLAMP
        )
        progressShader = shader
        progressPaint.shader = shader
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val f = rect.left
        val f2 = rect.top
        val f3 = rect.right
        val f4 = rect.bottom
        val f5 = cornerRadius

        // Track
        canvas.drawRoundRect(rect, f5, f5, trackPaint)

        // Text
        canvas.drawText(String.format(Locale.US, "%% %d", progress), xP, yProgress, textPaint)
        canvas.drawText(strHint, xH, yHint, textPaint)

        // Build outline path
        path.reset()
        partialPath.reset()
        val f6 = f + f5
        path.moveTo(f6, f2)
        path.lineTo(f3 - f5, f2)

        val f7 = 2f * f5
        val f8 = f3 - f7
        val f9 = f2 + f7
        path.arcTo(RectF(f8, f2, f3, f9), -90f, 90f, false)
        path.lineTo(f3, f4 - f5)

        val f10 = f4 - f7
        path.arcTo(RectF(f8, f10, f3, f4), 0f, 90f, false)
        path.lineTo(f6, f4)

        val f11 = f7 + f
        path.arcTo(RectF(f, f10, f11, f4), 90f, 90f, false)
        path.lineTo(f, f5 + f2)
        path.arcTo(RectF(f, f2, f11, f9), 180f, 90f, false)
        path.close()

        // Draw partial path for progress
        pathMeasure.setPath(path, false)
        pathMeasure.getSegment(
            0f,
            pathMeasure.length * (progress.toFloat() / maxProgress),
            partialPath,
            true
        )
        canvas.drawPath(partialPath, progressPaint)
    }
}
