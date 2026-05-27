package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View

class NeumorphicView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val rectF = RectF()
    private var baseColor = Color.rgb(200, 200, 200)
    private var darkShadowColor = 0
    private var lightHighlightColor = 0
    private var accentColor = 0
    private var textColor = 0
    private var iconColor = 0

    init {
        setBaseThemeColor(baseColor)
    }

    fun setBaseThemeColor(color: Int) {
        baseColor = color
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        darkShadowColor = Color.argb(150, maxOf(0, r - 50), maxOf(0, g - 50), maxOf(0, b - 50))
        lightHighlightColor = Color.argb(200, minOf(255, r + 50), minOf(255, g + 50), minOf(255, b + 50))
        accentColor = Color.rgb(maxOf(0, r - 30), maxOf(0, g - 30), maxOf(0, b - 30))
        textColor = Color.rgb(maxOf(0, r - 100), maxOf(0, g - 100), maxOf(0, b - 100))
        iconColor = textColor
        setBackgroundColor(baseColor)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val dp = resources.displayMetrics.density
        val margin = 30 * dp
        val w = width - margin * 2

        // Main rect
        drawNeumorphicRect(canvas, margin, margin, w, height - margin, 30 * dp,
            baseColor, darkShadowColor, lightHighlightColor, 10 * dp, true)

        // Circle
        val circleR = 100 * dp
        val centerX = width / 2f
        val circleY = margin + 100 * dp
        drawNeumorphicCircle(canvas, centerX, circleY, circleR,
            baseColor, lightHighlightColor, darkShadowColor, 10 * dp, false)

        // Surah name
        paint.color = textColor
        paint.textSize = 30 * dp
        paint.typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("الكَهْف", centerX, circleY + paint.textSize / 3f, paint)

        // English name
        paint.textSize = 20 * dp
        paint.typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        val bottomCircle = circleY + circleR
        canvas.drawText("Al- Kahfi", width / 2f, bottomCircle + 40 * dp, paint)

        paint.textSize = 16 * dp
        canvas.drawText("Ustadz : Muzammil Hasballah", width / 2f, bottomCircle + 65 * dp, paint)

        // Progress bar
        val barW = w - 60 * dp
        val barH = 20 * dp
        val barX = margin + 30 * dp
        val barY = bottomCircle + 115 * dp
        drawNeumorphicRect(canvas, barX, barY, barW, barH, 10 * dp,
            baseColor, darkShadowColor, lightHighlightColor, 5 * dp, true)

        // Accent progress
        paint.color = accentColor
        rectF.set(barX, barY, barX + barW * 0.6f, barY + barH)
        canvas.drawRoundRect(rectF, 10 * dp, 10 * dp, paint)
    }

    private fun drawNeumorphicRect(
        canvas: Canvas, x: Float, y: Float, w: Float, h: Float,
        radius: Float, base: Int, dark: Int, light: Int, offset: Float, isDark: Boolean
    ) {
        paint.color = if (isDark) dark else light
        rectF.set(x + offset, y + offset, x + w + offset, y + h + offset)
        canvas.drawRoundRect(rectF, radius, radius, paint)

        paint.color = if (isDark) light else dark
        rectF.set(x - offset, y - offset, x + w - offset, y + h - offset)
        canvas.drawRoundRect(rectF, radius, radius, paint)

        paint.color = base
        rectF.set(x, y, x + w, y + h)
        canvas.drawRoundRect(rectF, radius, radius, paint)
    }

    private fun drawNeumorphicCircle(
        canvas: Canvas, cx: Float, cy: Float, r: Float,
        base: Int, light: Int, dark: Int, offset: Float, isDark: Boolean
    ) {
        paint.color = if (isDark) light else dark
        canvas.drawCircle(cx + offset, cy + offset, r, paint)

        paint.color = if (isDark) dark else light
        canvas.drawCircle(cx - offset, cy - offset, r, paint)

        paint.color = base
        canvas.drawCircle(cx, cy, r, paint)
    }

    private fun createTrianglePath(x: Float, y: Float, size: Float, pointRight: Boolean): Path {
        val half = size / 2f
        return Path().apply {
            if (pointRight) {
                moveTo(x + half, y - half)
                lineTo(x - half, y)
                lineTo(x + half, y + half)
            } else {
                moveTo(x - half, y - half)
                lineTo(x + half, y)
                lineTo(x - half, y + half)
            }
            close()
        }
    }
}
