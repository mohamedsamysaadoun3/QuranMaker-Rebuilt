package hazem.nurmontage.videoquran.Utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Pair
import androidx.core.content.ContextCompat
import hazem.nurmontage.videoquran.constant.ResizeType

object Utils {

    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun f2(value: Float): Float {
        return Math.round(value * 100f) / 100f
    }

    fun countSpace(count: Int, text: String?): Int {
        if (text == null || count <= 0) return 0
        val limit = minOf(count, text.length)
        var spaces = 0
        for (i in 0 until limit) {
            if (text[i] == ' ') spaces++
        }
        return spaces
    }

    fun countSpace(text: String?): Int {
        if (text == null) return 0
        var count = 0
        for (i in text.indices) {
            if (text[i] == ' ') count++
        }
        return count
    }

    fun countIndex(startIndex: Int, spaceIndex: Int, text: String?): Int {
        if (text == null) {
            return if (startIndex < 0) 0 else text?.length ?: 0
        }
        if (startIndex < 0) return text.length

        var count = 0
        var i = startIndex
        while (i < text.length && count <= spaceIndex) {
            if (text[i] == ' ') count++
            i++
        }
        return i
    }

    fun countIndex(spaceIndex: Int, text: String): Int {
        var count = 0
        var i = 0
        while (i < text.length && count < spaceIndex) {
            if (text[i] == ' ') count++
            i++
        }
        return i
    }

    fun getDrawableByName(context: Context, name: String): Drawable? {
        val id = context.resources.getIdentifier(name, "drawable", context.packageName)
        return if (id != 0) ContextCompat.getDrawable(context, id) else null
    }

    fun getDimension(resizeType: ResizeType, size: Int): Pair<Int, Int> {
        val width: Int
        val height: Int
        when (resizeType) {
            ResizeType.SOCIAL_STORY -> {
                height = size
                width = (size * ResizeType.VERTICAL.value).toInt()
            }
            ResizeType.YOUTUBE_THUMBNAIL -> {
                height = size
                width = (size * ResizeType.YOUTUBE_THUMBNAIL.value).toInt()
            }
            else -> {
                width = size
                height = size
            }
        }
        return Pair(width, height)
    }

    fun isProbablyArabic(text: String): Boolean {
        var i = 0
        while (i < text.length) {
            val codePoint = text.codePointAt(i)
            if (codePoint in 1536..1760) return true
            i += Character.charCount(codePoint)
        }
        return false
    }

    fun indexOf(array: IntArray, value: Int): Int {
        for (i in array.indices) {
            if (array[i] == value) return i
        }
        return -1
    }
}
