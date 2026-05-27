package hazem.nurmontage.videoquran.common

import hazem.nurmontage.videoquran.constant.ResizeType
import hazem.nurmontage.videoquran.model.ItemDimension

object DataDimension {

    val dimensions: List<ItemDimension> = listOf(
        ItemDimension("Instagram Reels (9:16)", 0, ResizeType.STORY_9_16, 1080, 1920, 0),
        ItemDimension("TikTok (9:16)", 0, ResizeType.STORY_9_16, 1080, 1920, 1),
        ItemDimension("Youtube Short (9:16)", 0, ResizeType.STORY_9_16, 1080, 1920, 2),
        ItemDimension("Youtube (16:9)", 0, ResizeType.YOUTUBE_16_9, 1920, 1080, 3),
        ItemDimension("Square (1:1)", 0, ResizeType.SQUARE_1_1, 1080, 1080, 4),
        ItemDimension("Instagram Post (4:5)", 0, ResizeType.INSTAGRAM_4_5, 1080, 1350, 5),
    )

    fun getDimensionByType(type: ResizeType): ItemDimension? = dimensions.find { it.resizeType == type }
    fun getDimensionById(id: Int): ItemDimension? = dimensions.find { it.id == id }
}
