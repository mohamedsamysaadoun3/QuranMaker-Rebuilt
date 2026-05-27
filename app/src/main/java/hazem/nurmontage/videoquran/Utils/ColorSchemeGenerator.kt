package hazem.nurmontage.videoquran.Utils

import android.graphics.Color
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.max

object ColorSchemeGenerator {

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

    private fun colorToHSL(color: Int): FloatArray {
        val r = Color.red(color) / 255f
        val g = Color.green(color) / 255f
        val b = Color.blue(color) / 255f
        val max = max(r, max(g, b))
        val min = min(r, min(g, b))
        val l = (max + min) / 2f
        var h = 0f
        var s = 0f
        if (max != min) {
            val d = max - min
            s = if (l > 0.5f) d / (2f - max - min) else d / (max + min)
            h = when (max) {
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
