package hazem.nurmontage.videoquran.Utils

import java.util.Arrays

/**
 * Boyer-Moore string search optimized for Arabic text.
 * Used for Quran search functionality.
 *
 * Supports two usage patterns:
 * 1. Instance pattern: create with default constructor, call setmPattern(), then match(text)
 * 2. Constructor pattern: create with pattern, then match(text)
 * 3. Static pattern: call JavaBM.match(pattern, text) for one-off searches
 */
class JavaBM {

    private var mPattern: String = ""
    private var skipTable: IntArray = IntArray(42)

    constructor()

    constructor(pattern: String) {
        this.mPattern = pattern
        setSkipTable(pattern, skipTable)
    }

    fun setmPattern(pattern: String) {
        this.mPattern = pattern
        setSkipTable(pattern, skipTable)
    }

    fun getmPattern(): String = mPattern

    /**
     * Searches for the pattern in the given text.
     * @return the index of the first occurrence, or -1 if not found.
     */
    fun match(text: String): Int {
        var i = 0
        while (i <= text.length - mPattern.length) {
            var j = mPattern.length - 1
            var c = '\u0622' // Arabic letter Alef with Madda above
            while (true) {
                if (j < 0) {
                    break
                }
                val patternChar = mPattern[j]
                val textChar = text[i + j]
                if (patternChar != textChar) {
                    c = textChar
                    break
                }
                j--
                c = textChar
            }
            // Clamp character to Arabic range for skip table lookup
            if (c.code < 1570 || c.code > 1610) {
                c = 1611.toChar()
            }
            if (j < 0) {
                return i
            }
            i += maxOf(j - skipTable[c.code - 1570], 1)
        }
        return -1
    }

    private fun setSkipTable(pattern: String, table: IntArray) {
        Arrays.fill(table, -1)
        for (i in pattern.indices) {
            if (pattern[i].code < 1570 || pattern[i].code > 1610) {
                table[41] = i
            } else {
                table[pattern[i].code - 1570] = i
            }
        }
    }

    companion object {
        /**
         * Static method to find all occurrences of pattern in text.
         * @return list of start indices where pattern was found.
         */
        fun match(pattern: String, text: String): List<Int> {
            val results = mutableListOf<Int>()
            val patternLength = pattern.length
            val textLength = text.length
            val badCharShift = preprocessForBadCharacterShift(pattern)
            var j = patternLength - 1
            if (j >= textLength) {
                return results
            }
            var i = 0
            while (true) {
                if (j >= 0) {
                    val idx = i + j
                    if (idx >= textLength) break
                    val textChar = text[idx]
                    val patternChar = pattern[j]
                    if (textChar != patternChar) {
                        val shift = badCharShift[textChar]
                        i = if (shift == null) {
                            idx + 1
                        } else {
                            val s = idx - (shift + i)
                            if (s <= 0) 1 else s
                        } + i
                        j = patternLength - 1
                    } else {
                        if (j == 0) {
                            results.add(i)
                            i++
                            j = patternLength - 1
                        } else {
                            j--
                        }
                    }
                } else {
                    break
                }
            }
            return results
        }

        private fun preprocessForBadCharacterShift(pattern: String): Map<Char, Int> {
            val map = HashMap<Char, Int>()
            for (i in pattern.length - 1 downTo 0) {
                val c = pattern[i]
                if (!map.containsKey(c)) {
                    map[c] = i
                }
            }
            return map
        }
    }
}
