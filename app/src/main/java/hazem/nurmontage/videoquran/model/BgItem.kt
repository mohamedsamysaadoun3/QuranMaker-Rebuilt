package hazem.nurmontage.videoquran.model

import java.io.Serializable

data class BgItem(
    var id: Int = 0,
    var x: Float = 0f,
    var y: Float = 0f,
    var image: String = ""
) : Serializable
