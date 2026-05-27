package hazem.nurmontage.videoquran.model

import android.graphics.Canvas
import android.graphics.RectF
import java.io.Serializable

abstract class EntityView : Serializable {

    var rectF: MRectF = MRectF()
    var x: Float = 0f
    var y: Float = 0f
    var height: Float = 0f
    var scale: Float = 1.0f
    var factorSize: Float = 1.0f
    var startTime: Long = 0L
    var endTime: Long = 0L
    var isVisible: Boolean = true
    var entityIndex: Int = 0
    var entityTransition: Transition = Transition()


    fun getRectF(): RectF = RectF(rectF.left, rectF.top, rectF.right, rectF.bottom)

    fun setRectF(left: Float, top: Float, right: Float, bottom: Float) {
        rectF.left = left; rectF.top = top; rectF.right = right; rectF.bottom = bottom
    }

    abstract fun getType(): EntityType

    enum class EntityType { QURAN, TRANSLATION, BISMILAH, TEXT, SURAH_NAME }

    fun getRect(): RectF = getRectF()

    open fun setRect(rect: RectF) {
        rectF.left = rect.left
        rectF.top = rect.top
        rectF.right = rect.right
        rectF.bottom = rect.bottom
    }

    fun setPos(newX: Float, newY: Float) {
        val w = rectF.right - rectF.left
        val h = rectF.bottom - rectF.top
        rectF.left = newX
        rectF.top = newY
        rectF.right = newX + w
        rectF.bottom = newY + h
        x = newX
        y = newY
    }

    fun contains(px: Float, py: Float): Boolean {
        return rectF.left <= px && px <= rectF.right && rectF.top <= py && py <= rectF.bottom
    }

    open fun scale(scaleFactor: Float) {
        this.scale *= scaleFactor
        val cx = rectF.left + (rectF.right - rectF.left) / 2f
        val cy = rectF.top + (rectF.bottom - rectF.top) / 2f
        val newW = (rectF.right - rectF.left) * scaleFactor
        val newH = (rectF.bottom - rectF.top) * scaleFactor
        rectF.left = cx - newW / 2f
        rectF.top = cy - newH / 2f
        rectF.right = cx + newW / 2f
        rectF.bottom = cy + newH / 2f
    }

    fun setTransition(transitionType: Transition) {
        entityTransition = transitionType
    }

    fun getTransition(): Transition = entityTransition

    open fun draw(canvas: Canvas) {
        // Default no-op; subclasses override
    }

    fun setIndex(idx: Int) {
        entityIndex = idx
    }

    fun getIndex(): Int = entityIndex
}
