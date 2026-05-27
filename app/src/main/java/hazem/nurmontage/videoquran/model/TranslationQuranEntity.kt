package hazem.nurmontage.videoquran.model

import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import hazem.nurmontage.videoquran.constant.AyaTextPreset
import java.io.Serializable

class TranslationQuranEntity : EntityView(), Serializable {

    var text: String = ""
    var completeText: String = ""
    var fontName: String = "ReadexPro_Medium.ttf"
    var typeface: Typeface? = null
    var textColor: Int = Color.parseColor("#B0BEC5")
    var outlineColor: Int = Color.BLACK
    var bgColor: Int = Color.TRANSPARENT
    var outlineWidth: Float = 1.5f
    var shadowRadius: Float = 3f
    var shadowColor: Int = Color.BLACK
    var textSize: Float = 16f
    var opacity: Int = 255
    var preset: AyaTextPreset = AyaTextPreset.NONE
    var alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL
    var lineSpacing: Float = 1.2f
    var maxLines: Int = 0
    var fadeInDuration: Long = 0L
    var fadeOutDuration: Long = 0L
    var animationProgress: Float = 1f
    var bismilahTemplate: EntityBismilahTemplate? = null
    private var textPaint: TextPaint? = null
    var staticLayout: StaticLayout? = null
    var colorTrsl: Int = Color.parseColor("#B0BEC5")
    var factorSizeTrl: Float = 1.0f

    fun getTextPaint(): TextPaint {
        if (textPaint == null) {
            textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor; textSize = this@TranslationQuranEntity.textSize
                typeface = this@TranslationQuranEntity.typeface; alpha = opacity
            }
        }
        return textPaint!!
    }

    fun createLayout(availableWidth: Int) {
        val paint = getTextPaint()
        staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, paint, availableWidth)
            .setAlignment(alignment)
            .setLineSpacing(0f, lineSpacing)
            .setMaxLines(if (maxLines > 0) maxLines else Int.MAX_VALUE)
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
    override fun getType(): EntityType = EntityType.TRANSLATION
}
