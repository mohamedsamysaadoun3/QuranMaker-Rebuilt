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
}
