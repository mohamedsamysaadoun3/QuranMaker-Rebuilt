package hazem.nurmontage.videoquran.Utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Shader

object ArtistLightEffect {

    fun apply(bitmap: Bitmap, lightX: Float, lightY: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // Draw original bitmap
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        // Apply color matrix for warm tone
        val colorMatrix = ColorMatrix(floatArrayOf(
            1.05f, 0f, 0f, 0f, -6f,
            0f, 1.02f, 0f, 0f, -4f,
            0f, 0f, 0.95f, 0f, 10f,
            0f, 0f, 0f, 1f, 0f
        ))
        val colorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        canvas.drawBitmap(result, 0f, 0f, colorPaint)

        val w = width.toFloat()
        val h = height.toFloat()

        // Overlay radial gradient
        val saveCount1 = canvas.saveLayer(0f, 0f, w, h, null)
        val overlayGradient = RadialGradient(
            lightX, lightY,
            maxOf(width, height) * 0.45f,
            intArrayOf(Color.parseColor("#8844FFAA"), Color.parseColor("#33226655"), 0),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = overlayGradient
            xfermode = PorterDuffXfermode(PorterDuff.Mode.OVERLAY)
        }
        canvas.drawRect(0f, 0f, w, h, overlayPaint)
        canvas.restoreToCount(saveCount1)

        // Add glow effect
        val saveCount2 = canvas.saveLayer(0f, 0f, w, h, null)
        val glowGradient = RadialGradient(
            lightX, lightY,
            maxOf(width, height) * 0.25f,
            intArrayOf(Color.parseColor("#5533FFAA"), 0),
            null,
            Shader.TileMode.CLAMP
        )
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = glowGradient
            xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
        }
        canvas.drawRect(0f, 0f, w, h, glowPaint)
        canvas.restoreToCount(saveCount2)

        // Vignette effect
        val saveCount3 = canvas.saveLayer(0f, 0f, w, h, null)
        val vignetteGradient = RadialGradient(
            w / 2f, h / 2f,
            maxOf(width, height).toFloat(),
            intArrayOf(0, Color.parseColor("#44000000")),
            floatArrayOf(0.6f, 1f),
            Shader.TileMode.CLAMP
        )
        val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = vignetteGradient
            xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
        }
        canvas.drawRect(0f, 0f, w, h, vignettePaint)
        canvas.restoreToCount(saveCount3)

        return result
    }
}
