package hazem.nurmontage.videoquran.Utils

import android.graphics.Color
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.max

object ColorSchemeGenerator {

    data class Scheme(
        val primary: Int,
        val secondary: Int,
        val tertiary: Int,
        val label: Int,
        val background: Int,
        val surface: Int
    )

    fun generateScheme(color: Int, angle: Int = 0): Scheme {
        val hsl = colorToHSL(color)
        val h = hsl[0]
        val s = hsl[1]
        val l = hsl[2]

        // Generate harmonious colors
        val h2 = (h + 30) % 360
        val h3 = (h + 60) % 360
        val h4 = (h + 180) % 360

        val primary = color
        val secondary = hslToColor(h2, s.coerceAtMost(0.6f), l.coerceIn(0.3f, 0.7f))
        val tertiary = hslToColor(h3, s.coerceAtMost(0.5f), l.coerceIn(0.4f, 0.6f))

        // Label color: light text on dark backgrounds, dark text on light backgrounds
        val label = if (isColorDark(color)) Color.WHITE else Color.BLACK
        val background = hslToColor(h4, s.coerceAtMost(0.3f), 0.95f)
        val surface = ColorUtils.darken(color, 0.3f)

        return Scheme(
            primary = primary,
            secondary = secondary,
            tertiary = tertiary,
            label = label,
            background = background,
            surface = surface
        )
    }

    fun getComplementary(color: Int): Int {
        return Color.rgb(255 - Color.red(color), 255 - Color.green(color), 255 - Color.blue(color))
    }

    fun getAnalogous(color: Int, angle: Float = 30f): Pair<Int, Int> {
        val hsl = colorToHSL(color)
        val h1 = (hsl[0] + angle) % 360
        val h2 = (hsl[0] - angle + 360) % 360
        return Pair(hslToColor(h1, hsl[1], hsl[2]), hslToColor(h2, hsl[1], hsl[2]))
    }

    fun getTriadic(color: Int): Pair<Int, Int> {
        val hsl = colorToHSL(color)
        val h1 = (hsl[0] + 120) % 360
        val h2 = (hsl[0] + 240) % 360
        return Pair(hslToColor(h1, hsl[1], hsl[2]), hslToColor(h2, hsl[1], hsl[2]))
    }

    fun isColorDark(color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.5
    }

    private fun colorToHSL(color: Int): FloatArray {
        val r = Color.red(color) / 255f
        val g = Color.green(color) / 255f
        val b = Color.blue(color) / 255f
        val maxVal = max(r, max(g, b))
        val minVal = min(r, min(g, b))
        val l = (maxVal + minVal) / 2f
        var h = 0f
        var s = 0f
        if (maxVal != minVal) {
            val d = maxVal - minVal
            s = if (l > 0.5f) d / (2f - maxVal - minVal) else d / (maxVal + minVal)
            h = when (maxVal) {
                r -> (g - b) / d + (if (g < b) 6f else 0f)
                g -> (b - r) / d + 2f
                else -> (r - g) / d + 4f
            }
            h *= 60f
        }
        return floatArrayOf(h, s, l)
    }

    private fun hslToColor(h: Float, s: Float, l: Float): Int {
        if (s == 0f) {
            val v = (l * 255).toInt()
            return Color.rgb(v, v, v)
        }
        val q = if (l < 0.5f) l * (1 + s) else l + s - l * s
        val p = 2 * l - q
        return Color.rgb(
            hueToRGB(p, q, h / 360f + 1f / 3f),
            hueToRGB(p, q, h / 360f),
            hueToRGB(p, q, h / 360f - 1f / 3f)
        )
    }

    private fun hueToRGB(p: Float, q: Float, t: Float): Int {
        var tc = t
        if (tc < 0f) tc += 1f
        if (tc > 1f) tc -= 1f
        val color = when {
            tc < 1f / 6f -> p + (q - p) * 6f * tc
            tc < 1f / 2f -> q
            tc < 2f / 3f -> p + (q - p) * (2f / 3f - tc) * 6f
            else -> p
        }
        return (color * 255).toInt()
    }
}
