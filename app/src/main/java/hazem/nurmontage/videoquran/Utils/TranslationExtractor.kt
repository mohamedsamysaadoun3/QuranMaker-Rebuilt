package hazem.nurmontage.videoquran.Utils

import android.content.Context
import hazem.nurmontage.videoquran.Utils.QuranReader

/**
 * Extracts translation text from assets for specific ayah ranges.
 */
object TranslationExtractor {

    fun getTranslationForRange(
        context: Context,
        langCode: String,
        surahNumber: Int,
        startAyah: Int,
        endAyah: Int
    ): List<String> {
        val result = mutableListOf<String>()
        for (ayah in startAyah..endAyah) {
            val translation = QuranReader.getTranslation(context, langCode, surahNumber, ayah)
            if (translation.isNotEmpty()) {
                result.add(translation)
            }
        }
        return result
    }

    fun getCombinedTranslation(
        context: Context,
        langCode: String,
        surahNumber: Int,
        startAyah: Int,
        endAyah: Int
    ): String {
        return getTranslationForRange(context, langCode, surahNumber, startAyah, endAyah)
            .joinToString(" ")
    }
}
