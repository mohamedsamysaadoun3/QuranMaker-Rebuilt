package hazem.nurmontage.videoquran.model

import android.graphics.RectF
import java.io.Serializable

data class MRectF(
    var left: Float = 0f,
    var top: Float = 0f,
    var right: Float = 0f,
    var bottom: Float = 0f
) : Serializable {

    fun width(): Float = right - left
    fun height(): Float = bottom - top
    fun centerX(): Float = (left + right) / 2f
    fun centerY(): Float = (top + bottom) / 2f

    fun contains(x: Float, y: Float): Boolean = x >= left && x <= right && y >= top && y <= bottom

    fun offset(dx: Float, dy: Float) {
        left += dx
        top += dy
        right += dx
        bottom += dy
    }

    fun offsetTo(newLeft: Float, newTop: Float) {
        val w = width()
        val h = height()
        left = newLeft
        top = newTop
        right = newLeft + w
        bottom = newTop + h
    }

    fun set(l: Float, t: Float, r: Float, b: Float) {
        left = l
        top = t
        right = r
        bottom = b
    }

    fun toRectF(): RectF = RectF(left, top, right, bottom)

    companion object {
        fun from(rectF: RectF): MRectF = MRectF(rectF.left, rectF.top, rectF.right, rectF.bottom)
    }
}
