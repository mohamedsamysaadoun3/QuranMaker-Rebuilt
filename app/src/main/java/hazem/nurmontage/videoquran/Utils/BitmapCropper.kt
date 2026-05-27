package hazem.nurmontage.videoquran.Utils

import android.graphics.Bitmap
import hazem.nurmontage.videoquran.constant.ResizeType

object BitmapCropper {

    fun cropToRatio(bitmap: Bitmap, resizeType: ResizeType): Bitmap {
        val ratio = when (resizeType) {
            ResizeType.YOUTUBE_16_9 -> 16f / 9f
            ResizeType.STORY_9_16 -> 9f / 16f
            ResizeType.SQUARE_1_1 -> 1f
            ResizeType.INSTAGRAM_4_5 -> 4f / 5f
            else -> 9f / 16f
        }
        return cropToRatio(bitmap, ratio)
    }

    fun cropToRatio(bitmap: Bitmap, ratio: Float): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val currentRatio = w.toFloat() / h.toFloat()
        return if (currentRatio > ratio) {
            val newWidth = (h * ratio).toInt()
            val x = (w - newWidth) / 2
            Bitmap.createBitmap(bitmap, x, 0, newWidth, h)
        } else {
            val newHeight = (w / ratio).toInt()
            val y = (h - newHeight) / 2
            Bitmap.createBitmap(bitmap, 0, y, w, newHeight)
        }
    }
}
