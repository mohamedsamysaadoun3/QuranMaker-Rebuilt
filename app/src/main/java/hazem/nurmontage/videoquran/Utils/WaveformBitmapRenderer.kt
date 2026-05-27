package hazem.nurmontage.videoquran.Utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

/**
 * Renders audio waveform to bitmap for timeline display.
 */
object WaveformBitmapRenderer {

    fun render(
        amplitudes: FloatArray,
        width: Int,
        height: Int,
        color: Int = Color.parseColor("#522123"),
        bgColor: Int = Color.TRANSPARENT
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (bgColor != Color.TRANSPARENT) {
            canvas.drawColor(bgColor)
        }

        if (amplitudes.isEmpty()) return bitmap

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            strokeWidth = 2f
        }

        val barWidth = width.toFloat() / amplitudes.size
        val centerY = height / 2f

        for (i in amplitudes.indices) {
            val barHeight = amplitudes[i] * height / 2f
            val x = i * barWidth
            canvas.drawLine(x, centerY - barHeight, x, centerY + barHeight, paint)
        }

        return bitmap
    }
}
