package hazem.nurmontage.videoquran.Utils

/**
 * Quran text file utilities.
 * Replaces Bismilah phrase in Quran text files.
 */
object QuranFileUtils {

    private const val BISMILAH = "بِسْمِ اللَّهِ الرَّحْمَـٰنِ الرَّحِيمِ"
    private const val BISMILAH_SIMPLE = "بسم الله الرحمن الرحيم"

    fun removeBismilah(text: String): String {
        return text
            .replace(BISMILAH, "")
            .replace(BISMILAH_SIMPLE, "")
            .trim()
    }

    fun containsBismilah(text: String): Boolean {
        return text.contains(BISMILAH) || text.contains(BISMILAH_SIMPLE)
    }

    fun isBismilahOnly(text: String): Boolean {
        val cleaned = text.trim()
        return cleaned == BISMILAH || cleaned == BISMILAH_SIMPLE
    }
}
