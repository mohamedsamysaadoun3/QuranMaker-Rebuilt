package hazem.nurmontage.videoquran.Utils

import android.graphics.Bitmap
import android.graphics.Color

/**
 * Color conversion, darkening, lightening, and utility methods.
 */
object ColorUtils {

    fun darken(color: Int, factor: Float = 0.7f): Int {
        val r = (Color.red(color) * factor).toInt()
        val g = (Color.green(color) * factor).toInt()
        val b = (Color.blue(color) * factor).toInt()
        return Color.argb(Color.alpha(color), r, g, b)
    }

    // Alias for backward compatibility
    fun darkenColor(color: Int, factor: Float = 0.7f): Int = darken(color, factor)

    fun lighten(color: Int, factor: Float = 0.3f): Int {
        val r = Color.red(color) + ((255 - Color.red(color)) * factor).toInt()
        val g = Color.green(color) + ((255 - Color.green(color)) * factor).toInt()
        val b = Color.blue(color) + ((255 - Color.blue(color)) * factor).toInt()
        return Color.argb(Color.alpha(color), r.coerceAtMost(255), g.coerceAtMost(255), b.coerceAtMost(255))
    }

    // Alias for backward compatibility
    fun lightenColor(color: Int, factor: Float = 0.3f): Int = lighten(color, factor)

    fun withAlpha(color: Int, alpha: Int): Int {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    fun toHex(color: Int): String {
        return String.format("#%08X", color)
    }

    fun fromHex(hex: String): Int {
        return Color.parseColor(hex)
    }

    fun isLight(color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness < 0.5
    }

    fun isColorDark(color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.5
    }

    fun getEnergyColor(energy: Float): Int {
        // Green → Yellow → Red based on energy level (0.0 - 1.0)
        val e = energy.coerceIn(0f, 1f)
        val r = if (e < 0.5f) (e * 2 * 255).toInt() else 255
        val g = if (e < 0.5f) 255 else ((1 - (e - 0.5f) * 2) * 255).toInt()
        return Color.rgb(r, g, 0)
    }

    // Alias for BlurredImageView compatibility
    fun convertToEnergyColor(color: Int): Int {
        // Convert a color to its "energy" equivalent
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        val energy = (r + g + b) / (3f * 255f)
        return getEnergyColor(energy)
    }

    fun getComplementary(color: Int): Int {
        return Color.rgb(255 - Color.red(color), 255 - Color.green(color), 255 - Color.blue(color))
    }

    /**
     * Get the average (dominant) color from a bitmap.
     * Used by BlurredImageView for iPad frame coloring.
     */
    fun getAverageColor(bitmap: Bitmap): Int {
        val w = bitmap.width
        val h = bitmap.height
        // Sample a subset for performance
        val step = maxOf(1, (w * h) / 10000)
        var r = 0L
        var g = 0L
        var b = 0L
        var count = 0L
        for (y in 0 until h step step) {
            for (x in 0 until w step step) {
                val pixel = bitmap.getPixel(x, y)
                r += Color.red(pixel)
                g += Color.green(pixel)
                b += Color.blue(pixel)
                count++
            }
        }
        if (count == 0L) return Color.GRAY
        return Color.rgb((r / count).toInt(), (g / count).toInt(), (b / count).toInt())
    }
}
