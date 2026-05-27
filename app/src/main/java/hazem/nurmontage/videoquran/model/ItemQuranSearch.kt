package hazem.nurmontage.videoquran.model

import java.io.Serializable

/**
 * Represents a single Quran search result item.
 *
 * @param aya The full aya text.
 * @param surahName The name of the surah.
 * @param to The aya number within the surah.
 * @param surahIndex The zero-based index of the surah.
 * @param startSpannable The start index for highlighting the match within the aya text.
 * @param endSpannble The end index for highlighting the match within the aya text.
 */
class ItemQuranSearch(
    val aya: String,
    val surahName: String,
    val to: Int,
    val surahIndex: Int,
    val startSpannable: Int,
    val endSpannble: Int
) : Serializable
