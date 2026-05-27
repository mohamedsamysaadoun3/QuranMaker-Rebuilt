package hazem.nurmontage.videoquran.Utils

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ReplacementSpan

/**
 * Custom ReplacementSpan for ayah number ornaments (﴿١﴾ style).
 * Renders ayah numbers inside decorative brackets.
 */
class EndOfAyaSpan(
    private val ayahNumber: Int,
    private val textColor: Int = 0xFFD700.toInt(), // Gold
    private val bgColor: Int = 0x00000000, // Transparent
    private val textSize: Float = 14f
) : ReplacementSpan() {

    private val ayahText = "﴿${toArabicNumber(ayahNumber)}﴾"

    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        paint.textSize = textSize
        return paint.measureText(ayahText).toInt()
    }

    override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, baseline: Int, bottom: Int, paint: Paint) {
        paint.textSize = textSize
        paint.color = textColor
        canvas.drawText(ayahText, x, baseline.toFloat(), paint)
    }

    companion object {
        fun toArabicNumber(number: Int): String {
            val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
            return number.toString().map { arabicDigits[it.digitToInt()] }.joinToString("")
        }
    }
}
