package hazem.nurmontage.videoquran.Utils

import android.content.res.Resources
import android.graphics.Typeface
import android.text.TextUtils
import hazem.nurmontage.videoquran.common.Common
import java.util.TreeMap

/**
 * Quran font name ↔ Typeface mapping and loading utility.
 * Instance-based class matching the original Java API: FontProvider(Resources).
 * Maintains a sorted list of Quran font names and lazy-loads typefaces from assets.
 */
class FontProvider(resources: Resources) {

    private var fontNameToTypefaceFileQuran: MutableMap<String, String> = mutableMapOf()
    private var fontNamesQuran: MutableList<String> = mutableListOf()
    private var typefaces: MutableMap<String, Typeface> = mutableMapOf()
    private var resources: Resources? = resources
    private var defaultFontName: String? = null

    init {
        loadQuranFont()
    }

    private fun loadQuranFont() {
        fontNameToTypefaceFileQuran = mutableMapOf(
            "المجد" to "المجد.ttf",
            "جنة" to "جنة.ttf",
            "محمدي" to "محمدي.ttf",
            "خط الثلث مزخرف" to "الثلث مزخرف.ttf",
            "باك تايب أجراك" to "باك تايب أجراك.ttf",
            "باك تايب تحرير" to "باك تايب تحرير.ttf",
            "باك تايب نسخ" to "باك تايب نسخ.ttf",
            "خط نسخ عثماني" to "خط نسخ عثماني.otf",
            "عثماني" to Common.FONT_QURAN,
            "خط القيروان" to "خط القيروان.ttf",
            "خط حفص" to "خط حفص.ttf",
            "خط ورش" to "خط ورش.ttf",
            "قالون" to "قالون.ttf",
            "مريم" to "مريم.ttf",
            "الأقصى" to "الأقصى.ttf",
            "أجنادين" to "أجنادين.ttf",
            "بيبو" to "بيبو.ttf",
            "بيسان لايت" to "بيسان لايت.ttf",
            "تبيان" to "تبيان.ttf",
            "تجمع كوفي" to "تجمع كوفي.ttf",
            "تريكا" to "تريكا.ttf",
            "خط تجمع المصممين" to "خط تجمع المصممين.ttf",
            "شمائل" to "شمائل.ttf",
            "عصومي" to "عصومي.ttf",
            "فرشة" to "فرشة.ttf",
            "فسيح" to "فسيح.ttf",
            "كوفي" to "كوفي.ttf",
            "مطرية" to "مطرية.ttf",
            "نمر" to "نمر.ttf",
            "هيفن" to "هيفن.ttf",
            "لفتا بلاك" to "لفتا بلاك.otf",
            "خط الإبل" to "خط الإبل.otf",
        )
        // Sorted list of font names (matching original TreeSet behavior)
        fontNamesQuran = TreeMap(fontNameToTypefaceFileQuran).keys.toList().toMutableList()
    }

    fun getFullName(name: String): String? = fontNameToTypefaceFileQuran[name]

    fun getTypeface(name: String?): Typeface {
        if (name == null || TextUtils.isEmpty(name)) return Typeface.DEFAULT
        typefaces[name]?.let { return it }
        return try {
            val assetPath = "fonts/arabic/${fontNameToTypefaceFileQuran[name]}"
            val tf = Typeface.createFromAsset(resources?.assets, assetPath)
            typefaces[name] = tf
            tf
        } catch (_: Exception) {
            Typeface.DEFAULT
        }
    }

    fun clear() {
        fontNameToTypefaceFileQuran.clear()
        fontNameToTypefaceFileQuran = mutableMapOf()
        fontNamesQuran.clear()
        fontNamesQuran = mutableListOf()
        typefaces.clear()
        typefaces = mutableMapOf()
        resources = null
    }

    fun getFontNamesQuran(): List<String> = fontNamesQuran

    fun setDefaultFontName(name: String?) { defaultFontName = name }
    fun getDefaultFontName(): String? = defaultFontName

    fun getResources(): Resources? = resources
}
