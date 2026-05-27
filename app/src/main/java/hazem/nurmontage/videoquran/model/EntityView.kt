package hazem.nurmontage.videoquran.model

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
    var index: Int = 0

    fun getRectF(): RectF = RectF(rectF.left, rectF.top, rectF.right, rectF.bottom)

    fun setRectF(left: Float, top: Float, right: Float, bottom: Float) {
        rectF.left = left; rectF.top = top; rectF.right = right; rectF.bottom = bottom
    }

    abstract fun getType(): EntityType

    enum class EntityType { QURAN, TRANSLATION, BISMILAH, TEXT, SURAH_NAME }
}
