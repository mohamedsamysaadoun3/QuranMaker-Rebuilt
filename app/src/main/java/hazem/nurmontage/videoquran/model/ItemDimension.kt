package hazem.nurmontage.videoquran.model

import hazem.nurmontage.videoquran.constant.ResizeType

data class ItemDimension(
    val name: String = "",
    val image: Int = 0,
    val resizeType: ResizeType = ResizeType.STORY_9_16,
    val width: Int = 0,
    val height: Int = 0,
    val id: Int = 0
)
