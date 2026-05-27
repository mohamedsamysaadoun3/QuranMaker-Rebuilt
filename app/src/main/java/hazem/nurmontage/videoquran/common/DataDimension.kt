package hazem.nurmontage.videoquran.common

import android.content.res.Resources
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.constant.ResizeType
import hazem.nurmontage.videoquran.model.ItemDimension

object DataDimension {

    val dimensions: List<ItemDimension> = listOf(
        ItemDimension("Instagram Reels (9:16)", 0, ResizeType.STORY_9_16, 1080, 1920, "ir_9:16"),
        ItemDimension("TikTok (9:16)", 0, ResizeType.STORY_9_16, 1080, 1920, "t"),
        ItemDimension("Youtube Short (9:16)", 0, ResizeType.STORY_9_16, 1080, 1920, "y_9:16"),
        ItemDimension("Youtube (16:9)", 0, ResizeType.YOUTUBE_16_9, 1920, 1080, "y_16:9"),
        ItemDimension("Square (1:1)", 0, ResizeType.SQUARE_1_1, 1080, 1080, "i_1:1"),
        ItemDimension("Instagram Post (4:5)", 0, ResizeType.INSTAGRAM_4_5, 1080, 1350, "ip_4:5"),
    )

    /**
     * Returns the full list of dimension presets with resolved string resources.
     * Matches the original Java `DataDimension.getALl(Resources)` API.
     */
    fun getALl(resources: Resources): List<ItemDimension> = listOf(
        ItemDimension(resources.getString(R.string.tiktok), R.drawable.ic_tiktok, ResizeType.SOCIAL_STORY, 720, 1280, "t"),
        ItemDimension(resources.getString(R.string.youtube_thumbnail), R.drawable.ic_youtube, ResizeType.YOUTUBE_THUMBNAIL, 1280, 720, "y_16:9"),
        ItemDimension(resources.getString(R.string.youtube_short), R.drawable.ic_youtube_shorts_icon, ResizeType.SOCIAL_STORY, 720, 1280, "y_9:16"),
        ItemDimension(resources.getString(R.string.instagram_post), R.drawable.ic_instagram, ResizeType.SQUARE, 1080, 1080, "i_1:1"),
        ItemDimension(resources.getString(R.string.instagram_story), R.drawable.ic_instagram, ResizeType.SOCIAL_STORY, 720, 1280, "i_9:16"),
    )

    fun getDimensionByType(type: ResizeType): ItemDimension? = dimensions.find { it.resizeType == type }
    fun getDimensionById(id: String): ItemDimension? = dimensions.find { it.id == id }
}
