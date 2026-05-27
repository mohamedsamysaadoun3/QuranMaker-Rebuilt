package hazem.nurmontage.videoquran.model

data class ItemQuranSearch(
    val aya: String = "",
    val surahName: String = "",
    val surahNumber: Int = 0,
    val ayaNumber: Int = 0,
    val startIndex: Int = 0,
    val endIndex: Int = 0
)
