package hazem.nurmontage.videoquran.Utils

/**
 * Removes Arabic diacritical marks (tashkeel/harakat) from text.
 * Used for searching Quran text without tashkeel.
 */
object RemoveTashkeel {

    // Arabic diacritical marks Unicode range
    private val tashkeelRegex = Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06DC\\u06DF-\\u06E4\\u06E7\\u06E8\\u06EA-\\u06ED]")

    fun remove(text: String): String {
        return tashkeelRegex.replace(text, "")
    }

    fun removeKeepShadda(text: String): String {
        // Keep shadda (U+0651) but remove other tashkeel
        val regex = Regex("[\\u0610-\\u061A\\u064B-\\u0650\\u0652-\\u065F\\u0670\\u06D6-\\u06DC\\u06DF-\\u06E4\\u06E7\\u06E8\\u06EA-\\u06ED]")
        return regex.replace(text, "")
    }
}
