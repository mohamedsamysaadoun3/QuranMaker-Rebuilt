package hazem.nurmontage.videoquran.Utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RadialGradient
import android.graphics.Shader

/**
 * Cinematic color grading + vignette effect on bitmaps.
 */
object CinematicProcessor {

    fun applyVignette(bitmap: Bitmap, intensity: Float = 0.5f): Bitmap {
        val output = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)
        val centerX = bitmap.width / 2f
        val centerY = bitmap.height / 2f
        val radius = Math.max(centerX, centerY)

        val vignette = RadialGradient(
            centerX, centerY, radius * 0.5f,
            intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT, 0x88000000.toInt()),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        val paint = Paint().apply { shader = vignette }
        canvas.drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paint)
        return output
    }

    fun applyColorGrading(bitmap: Bitmap, tintColor: Int, alpha: Int = 40): Bitmap {
        val output = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)
        val paint = Paint().apply {
            colorFilter = PorterDuffColorFilter(tintColor, PorterDuff.Mode.OVERLAY)
            this.alpha = alpha
        }
        canvas.drawBitmap(output, 0f, 0f, paint)
        return output
    }
}
