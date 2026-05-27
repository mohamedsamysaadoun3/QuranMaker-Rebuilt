package hazem.nurmontage.videoquran.Utils

import android.graphics.LinearGradient
import android.graphics.RectF
import android.graphics.Shader
import hazem.nurmontage.videoquran.model.Gradient

object CreateGradient {

    fun createLinearGradient(width: Int, height: Int, gradient: Gradient): LinearGradient {
        val (x0, y0, x1, y1) = calculateGradientPoints(width.toFloat(), height.toFloat(), gradient.angle.toFloat())
        return LinearGradient(
            x0, y0, x1, y1,
            intArrayOf(gradient.startColor, gradient.centerColor, gradient.endColor),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
    }

    fun createLinearGradient(width: Int, height: Int, startColor: Int, endColor: Int): LinearGradient {
        return LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), startColor, endColor, Shader.TileMode.CLAMP)
    }

    /**
     * Create a linear gradient within a RectF with a specified angle.
     * Used by BlurredImageView for iPad frame gradients.
     */
    fun createLinearGradientWithAngle(
        rect: RectF,
        angle: Int,
        colors: IntArray,
        positions: FloatArray?
    ): LinearGradient {
        val (x0, y0, x1, y1) = calculateGradientPoints(rect.width(), rect.height(), angle.toFloat())
        return LinearGradient(
            rect.left + x0, rect.top + y0,
            rect.left + x1, rect.top + y1,
            colors,
            positions,
            Shader.TileMode.CLAMP
        )
    }

    private fun calculateGradientPoints(width: Float, height: Float, angle: Float): FloatArray {
        val angleRad = Math.toRadians(angle.toDouble())
        val centerX = width / 2f
        val centerY = height / 2f
        val diagonal = Math.sqrt((width * width + height * height).toDouble()).toFloat() / 2f
        val x0 = centerX - Math.sin(angleRad).toFloat() * diagonal
        val y0 = centerY - Math.cos(angleRad).toFloat() * diagonal
        val x1 = centerX + Math.sin(angleRad).toFloat() * diagonal
        val y1 = centerY + Math.cos(angleRad).toFloat() * diagonal
        return floatArrayOf(x0, y0, x1, y1)
    }
}
