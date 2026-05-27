package hazem.nurmontage.videoquran.Utils

import android.content.Context
import android.graphics.Typeface
import java.io.File

/**
 * Quran font name ↔ Typeface mapping and loading utility.
 * Maps font display names to asset file paths.
 */
object FontProvider {

    private val fontCache = mutableMapOf<String, Typeface?>()

    // Font display name → asset path mapping
    private val fontMap = mapOf(
        // Arabic fonts
        "عثماني" to "fonts/arabic/عثماني.otf",
        "خط الإبل" to "fonts/arabic/خط الإبل.otf",
        "خط القلم" to "fonts/arabic/خط القلم.ttf",
        "خط النسخ" to "fonts/arabic/خط النسخ.ttf",
        "خط المصحف" to "fonts/arabic/خط المصحف.ttf",
        "خط القرآن طه" to "fonts/arabic/خط القرآن طه.ttf",
        "خط حفص" to "fonts/arabic/خط حفص.ttf",
        "خط ورش" to "fonts/arabic/خط ورش.ttf",
        "كوفي" to "fonts/arabic/كوفي.ttf",
        "نسخ" to "fonts/arabic/نسخ.ttf",
        "نون" to "fonts/arabic/نون.ttf",
        "قرآن" to "fonts/arabic/قرآن.ttf",
        "مريم" to "fonts/arabic/مريم.ttf",
        "هيفن" to "fonts/arabic/هيفن.ttf",
        "فسيح" to "fonts/arabic/فسيح.ttf",
        "نور الهدى" to "fonts/arabic/نور الهدى.ttf",
        "بيسان" to "fonts/arabic/بيسان.ttf",
        "تبيان" to "fonts/arabic/تبيان.ttf",
        "شأمائل" to "fonts/arabic/شمائل.ttf",
        "الأقصى" to "fonts/arabic/الأقصى.ttf",
        "الثلث" to "fonts/arabic/الثلث.ttf",
        "القلم" to "fonts/arabic/القلم.ttf",
        "المجد" to "fonts/arabic/المجد.ttf",
        "قالون" to "fonts/arabic/قالون.ttf",
        "سالم قرآن" to "fonts/arabic/سالم قرآن.ttf",
        "نبي" to "fonts/arabic/نبي.ttf",
        // Latin fonts
        "Poppins" to "fonts/Poppins-Regular.ttf",
        "ReadexPro" to "fonts/ReadexPro-Regular.ttf",
        "Alegreya" to "fonts/Alegreya-Regular.ttf",
        "Rubik" to "fonts/Rubik-SemiBold.ttf",
    )

    fun getTypeface(context: Context, fontName: String): Typeface? {
        if (fontCache.containsKey(fontName)) return fontCache[fontName]

        val assetPath = fontMap[fontName] ?: findAssetPath(fontName)
        val typeface = try {
            if (assetPath != null) Typeface.createFromAsset(context.assets, assetPath) else null
        } catch (_: Exception) {
            try {
                Typeface.createFromFile(File(fontName))
            } catch (_: Exception) { null }
        }
        fontCache[fontName] = typeface
        return typeface
    }

    private fun findAssetPath(fontName: String): String? {
        // Try common locations
        val candidates = listOf(
            "fonts/arabic/$fontName.ttf",
            "fonts/arabic/$fontName.otf",
            "fonts/$fontName.ttf",
            "fonts/$fontName.otf",
        )
        return candidates.firstOrNull()
    }

    fun getAllFontNames(): List<String> = fontMap.keys.toList()

    fun clearCache() {
        fontCache.clear()
    }
}
