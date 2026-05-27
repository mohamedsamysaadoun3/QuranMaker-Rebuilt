package hazem.nurmontage.videoquran.model

import hazem.nurmontage.videoquran.constant.ResizeType

/**
 * Represents a video output dimension preset (e.g. TikTok 9:16, YouTube 16:9).
 * Converted from original Java decompiled source.
 */
data class ItemDimension(
    val name: String = "",
    val image: Int = 0,
    val resizeType: ResizeType = ResizeType.STORY_9_16,
    val w: Int = 0,
    val h: Int = 0,
    val id: String = ""
) {
    /** Convenience accessor for layout resource */
    fun getLayout(): Int = 0
}