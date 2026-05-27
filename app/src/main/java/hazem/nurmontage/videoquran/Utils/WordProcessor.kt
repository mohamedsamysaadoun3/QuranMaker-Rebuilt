package hazem.nurmontage.videoquran.Utils

import hazem.nurmontage.videoquran.model.WordModel

object WordProcessor {

    fun mapIndexAfterGroupReverse(index: Int, groupSize: Int, totalLength: Int): Int {
        val groupStart = (index / groupSize) * groupSize
        return groupStart + (minOf(groupSize, totalLength - groupStart) - 1) - (index % groupSize)
    }

    fun reverseInGroupsOfFour(words: List<WordModel>): List<WordModel> {
        val result = mutableListOf<WordModel>()
        var i = 0
        while (i < words.size) {
            val end = minOf(i + 4, words.size)
            val group = words.subList(i, end).toMutableList()
            group.reverse()
            result.addAll(group)
            i = end
        }
        return result
    }

    fun findAndSelectPhrase(fullText: String, phrase: String): List<WordModel> {
        val words = fullText.trim().split("\\s+".toRegex())
        val phraseWords = phrase.trim().split("\\s+".toRegex())
        val result = mutableListOf<WordModel>()

        var matchStart = -1
        outer@ for (i in 0..words.size - phraseWords.size) {
            for (j in phraseWords.indices) {
                if (words[i + j] != phraseWords[j]) continue@outer
            }
            matchStart = i
            break@outer
        }

        for (i in words.indices) {
            val isSelected = matchStart != -1 && i >= matchStart && i < phraseWords.size + matchStart
            result.add(WordModel(words[i], isSelected))
        }
        return result
    }
}
