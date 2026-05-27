package hazem.nurmontage.videoquran.Utils

/**
 * Boyer-Moore string search optimized for Arabic text.
 * Used for Quran search functionality.
 */
class JavaBM(private val pattern: String) {

    private val patternLength = pattern.length
    private val badCharShift = IntArray(65536) { patternLength }

    init {
        for (i in 0 until patternLength - 1) {
            val char = pattern[i].code
            if (char < badCharShift.size) {
                badCharShift[char] = patternLength - 1 - i
            }
        }
    }

    fun search(text: String): List<Int> {
        val results = mutableListOf<Int>()
        if (patternLength == 0 || text.length < patternLength) return results

        var i = 0
        while (i <= text.length - patternLength) {
            var j = patternLength - 1
            while (j >= 0 && pattern[j] == text[i + j]) {
                j--
            }
            if (j < 0) {
                results.add(i)
                i += patternLength
            } else {
                val shift = text[i + j].code
                i += if (shift < badCharShift.size) badCharShift[shift] else patternLength
            }
        }
        return results
    }
}
