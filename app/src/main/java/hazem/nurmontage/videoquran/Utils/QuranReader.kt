package hazem.nurmontage.videoquran.Utils

import android.content.Context

/**
 * Reads ayah text and translations from Quran asset files.
 * Quran files are stored in assets/quran/ as text files.
 */
object QuranReader {

    private val quranCache = mutableMapOf<String, List<String>>()

    /**
     * Read a Quran text file from assets.
     * @param context Android context
     * @param fileName e.g. "quran-simple.txt", "en.hilali.txt"
     * @return List of lines (ayahs), indexed by ayah number
     */
    fun readQuranFile(context: Context, fileName: String): List<String> {
        if (quranCache.containsKey(fileName)) return quranCache[fileName]!!

        val lines = try {
            context.assets.open("quran/$fileName").bufferedReader().use { it.readLines() }
        } catch (_: Exception) {
            emptyList()
        }
        quranCache[fileName] = lines
        return lines
    }

    /**
     * Get a specific ayah from the simple Quran text.
     * @param surahNumber 1-114
     * @param ayahNumber ayah number within the surah
     */
    fun getAyah(context: Context, surahNumber: Int, ayahNumber: Int): String {
        val lines = readQuranFile(context, "quran-simple.txt")
        val index = getAyahIndex(surahNumber, ayahNumber) ?: return ""
        return lines.getOrElse(index) { "" }
    }

    /**
     * Get translation for a specific ayah.
     * @param langCode e.g. "en", "fr", "ar"
     */
    fun getTranslation(context: Context, langCode: String, surahNumber: Int, ayahNumber: Int): String {
        val fileName = findTranslationFile(langCode) ?: return ""
        val lines = readQuranFile(context, fileName)
        val index = getAyahIndex(surahNumber, ayahNumber) ?: return ""
        return lines.getOrElse(index) { "" }
    }

    /**
     * Get all ayahs in a surah range.
     */
    fun getAyahRange(context: Context, surahNumber: Int, startAyah: Int, endAyah: Int): List<String> {
        val lines = readQuranFile(context, "quran-simple.txt")
        val result = mutableListOf<String>()
        for (ayah in startAyah..endAyah) {
            val index = getAyahIndex(surahNumber, ayah) ?: continue
            lines.getOrNull(index)?.let { result.add(it) }
        }
        return result
    }

    private fun findTranslationFile(langCode: String): String? {
        return when (langCode) {
            "ar" -> "ar.muyassar.txt"
            "en" -> "en.hilali.txt"
            "fr" -> "fr.hamidullah.txt"
            "de" -> "de.bubenheim.txt"
            "id" -> "id.indonesian.txt"
            "tr" -> "tr.ozturk.txt"
            "ur" -> "ur.maududi.txt"
            "bn" -> "bn.bengali.txt"
            "fa" -> "fa.fooladvand.txt"
            else -> null
        }
    }

    /**
     * Calculate global ayah index from surah and ayah numbers.
     * Uses standard Quran ayah counting.
     */
    private fun getAyahIndex(surahNumber: Int, ayahNumber: Int): Int? {
        // Ayah offsets per surah (cumulative count of ayahs before each surah)
        val surahOffsets = intArrayOf(
            0, 7, 293, 493, 669, 789, 954, 1160, 1235, 1364, 1473, 1596, 1707, 1750, 1802, 1901,
            2029, 2140, 2250, 2348, 2483, 2595, 2673, 2791, 2855, 2932, 3159, 3247, 3366, 3563,
            3660, 3705, 3788, 3970, 4059, 4133, 4218, 4272, 4325, 4414, 4473, 4510, 4545, 4612,
            4667, 4702, 4754, 4846, 4884, 4919, 4961, 5005, 5075, 5104, 5126, 5150, 5163, 5177,
            5188, 5199, 5211, 5221, 5231, 5241, 5250, 5259, 5267, 5273, 5279, 5285, 5289, 5293,
            5297, 5301, 5306, 5311, 5315, 5319, 5323, 5327, 5331, 5335, 5339, 5342, 5345, 5348,
            5351, 5354, 5357, 5360, 5362, 5365, 5368, 5370, 5373, 5375, 5378, 5380, 5383, 5385,
            5387, 5389, 5391, 5393, 5395, 5397, 5399, 5401, 5403, 5405, 5407, 5409, 5411, 5413,
            5415, 5417, 5419, 5421, 5423, 5425, 5427, 5429, 5431, 5433, 5435, 5437, 5439, 6236
        )
        if (surahNumber < 1 || surahNumber > 114) return null
        val offset = surahOffsets[surahNumber - 1]
        return offset + ayahNumber - 1
    }

    fun clearCache() {
        quranCache.clear()
    }

    fun getAyahText(context: Context, surah: Int, aya: Int): String? {
        val lines = readQuranFile(context, "quran-simple.txt")
        val index = getAyahIndex(surah, aya) ?: return null
        return lines.getOrElse(index) { null }
    }
    fun getTranslationAyahText(surah: Int, aya: Int): String? {
        // Default to English translation when no context available
        return null
    }

    fun getTranslationAyahText(context: Context, fileName: String, surah: Int, aya: Int): String? {
        val lines = readQuranFile(context, fileName)
        val index = getAyahIndex(surah, aya) ?: return null
        return lines.getOrElse(index) { null }
    }
}
