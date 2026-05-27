package hazem.nurmontage.videoquran.model

import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import hazem.nurmontage.videoquran.constant.AyaTextPreset
import java.io.Serializable

class BismilahEntity : EntityView(), Serializable {

    var text: String = "بسم الله الرحمن الرحيم"
    var fontName: String = "خط الإبل.otf"
    var typeface: Typeface? = null
    var textColor: Int = Color.WHITE
    var outlineColor: Int = Color.BLACK
    var bgColor: Int = Color.TRANSPARENT
    var outlineWidth: Float = 2f
    var shadowRadius: Float = 4f
    var shadowColor: Int = Color.BLACK
    var glowRadius: Float = 8f
    var glowColor: Int = Color.WHITE
    var textSize: Float = 28f
    var opacity: Int = 255
    var preset: AyaTextPreset = AyaTextPreset.NONE
    var alignment: Layout.Alignment = Layout.Alignment.ALIGN_CENTER
    var lineSpacing: Float = 1.2f
    var fadeInDuration: Long = 0L
    var fadeOutDuration: Long = 0L
    var animationProgress: Float = 1f
    private var textPaint: TextPaint? = null
    var staticLayout: StaticLayout? = null

    fun getTextPaint(): TextPaint {
        if (textPaint == null) {
            textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor; textSize = this@BismilahEntity.textSize
                typeface = this@BismilahEntity.typeface; alpha = opacity
            }
        }
        return textPaint!!
    }

    fun createLayout(availableWidth: Int) {
        val paint = getTextPaint()
        staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, paint, availableWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, lineSpacing)
            .build()
    }

    fun draw(canvas: Canvas, offsetX: Float, offsetY: Float) {
        val layout = staticLayout ?: return
        canvas.save()
        canvas.translate(offsetX + x, offsetY + y)
        layout.draw(canvas)
        canvas.restore()
    }

    fun invalidatePaints() { textPaint = null }
    override fun getType(): EntityType = EntityType.BISMILAH
}
