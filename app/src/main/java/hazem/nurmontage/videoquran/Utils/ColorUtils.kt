package hazem.nurmontage.videoquran.Utils

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

    fun lighten(color: Int, factor: Float = 0.3f): Int {
        val r = Color.red(color) + ((255 - Color.red(color)) * factor).toInt()
        val g = Color.green(color) + ((255 - Color.green(color)) * factor).toInt()
        val b = Color.blue(color) + ((255 - Color.blue(color)) * factor).toInt()
        return Color.argb(Color.alpha(color), r.coerceAtMost(255), g.coerceAtMost(255), b.coerceAtMost(255))
    }

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

    fun getEnergyColor(energy: Float): Int {
        // Green → Yellow → Red based on energy level (0.0 - 1.0)
        val e = energy.coerceIn(0f, 1f)
        val r = if (e < 0.5f) (e * 2 * 255).toInt() else 255
        val g = if (e < 0.5f) 255 else ((1 - (e - 0.5f) * 2) * 255).toInt()
        return Color.rgb(r, g, 0)
    }

    fun getComplementary(color: Int): Int {
        return Color.rgb(255 - Color.red(color), 255 - Color.green(color), 255 - Color.blue(color))
    }
}
