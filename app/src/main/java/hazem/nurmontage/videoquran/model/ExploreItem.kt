package hazem.nurmontage.videoquran.model

import java.io.Serializable

data class ExploreItem(
    var path: String = "",
    var size: Long = 0,
    var name: String = "",
    var isFolder: Boolean = false
) : Serializable
