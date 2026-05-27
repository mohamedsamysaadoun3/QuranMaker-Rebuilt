package hazem.nurmontage.videoquran.Utils

import hazem.nurmontage.videoquran.constant.ResizeType

object AspectRatioCalculator {

    fun calculate(resizeType: ResizeType, baseWidth: Int): Pair<Int, Int> {
        val ratio = when (resizeType) {
            ResizeType.YOUTUBE_16_9 -> 16f / 9f
            ResizeType.STORY_9_16 -> 9f / 16f
            ResizeType.SQUARE_1_1 -> 1f
            ResizeType.INSTAGRAM_4_5 -> 4f / 5f
            else -> 9f / 16f
        }
        val height = (baseWidth / ratio).toInt()
        return Pair(baseWidth, height)
    }

    fun getStandardResolution(resizeType: ResizeType): Pair<Int, Int> {
        return when (resizeType) {
            ResizeType.YOUTUBE_16_9 -> Pair(1920, 1080)
            ResizeType.STORY_9_16 -> Pair(1080, 1920)
            ResizeType.SQUARE_1_1 -> Pair(1080, 1080)
            ResizeType.INSTAGRAM_4_5 -> Pair(1080, 1350)
            else -> Pair(1080, 1920)
        }
    }

    /**
     * Calculate width from height for SOCIAL_STORY aspect ratio (9:16).
     */
    fun calculateWidth(height: Int): Int {
        // 9:16 ratio -> width = height * 9 / 16
        return (height * 9f / 16f).toInt()
    }

    /**
     * Calculate height from width for YouTube (16:9) aspect ratio.
     */
    fun calculateHeight_Youtube(width: Int): Int {
        // 16:9 ratio -> height = width * 9 / 16
        return (width * 9f / 16f).toInt()
    }

    /**
     * Calculate height from width for social story (9:16) aspect ratio.
     */
    fun calculateHeight_Story(width: Int): Int {
        // 9:16 ratio -> height = width * 16 / 9
        return (width * 16f / 9f).toInt()
    }
}
