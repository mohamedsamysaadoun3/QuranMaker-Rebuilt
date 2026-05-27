package hazem.nurmontage.videoquran.model

import java.io.Serializable

data class RecitersModel(
    var identifier: String = "",
    var surahIndex: Int = 0,
    var numberAya: Int = 0,
    var isTarteel: Boolean = false
) : Serializable
