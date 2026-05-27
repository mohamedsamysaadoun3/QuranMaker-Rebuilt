package hazem.nurmontage.videoquran.model

import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import hazem.nurmontage.videoquran.constant.AyaTextPreset
import java.io.Serializable

class QuranEntity : EntityView(), Serializable {

    var aya: String = ""
    var completeAya: String = ""
    var translation: String? = null
    var translationComplete: String? = null
    var fontName: String = "عثماني.otf"
    var translationFont: String = "ReadexPro_Medium.ttf"
    var typeface: Typeface? = null
    var translationTypeface: Typeface? = null
    var textColor: Int = Color.WHITE
    var outlineColor: Int = Color.BLACK
    var shadowColor: Int = Color.BLACK
    var glowColor: Int = Color.WHITE
    var bgColor: Int = Color.TRANSPARENT
    var translationColor: Int = Color.parseColor("#B0BEC5")
    var textSize: Float = 24f
    var translationTextSize: Float = 16f
    var outlineWidth: Float = 2f
    var shadowRadius: Float = 4f
    var glowRadius: Float = 8f
    var preset: AyaTextPreset = AyaTextPreset.NONE
    var opacity: Int = 255
    var alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL
    var lineSpacing: Float = 1.2f
    var maxLines: Int = 0
    var icon: String = "hafes"
    var iconResource: Int = 0
    var startWordIndex: Int = 0
    var endWordIndex: Int = 0
    var highlightColor: Int = Color.YELLOW
    var fadeInDuration: Long = 0L
    var fadeOutDuration: Long = 0L
    var animationProgress: Float = 1f
    private var textPaint: TextPaint? = null
    private var outlinePaint: TextPaint? = null
    var staticLayout: StaticLayout? = null
    var translationLayout: StaticLayout? = null

    fun getTextPaint(): TextPaint {
        if (textPaint == null) {
            textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor; textSize = this@QuranEntity.textSize
                typeface = this@QuranEntity.typeface; alpha = opacity
            }
        }
        return textPaint!!
    }

    fun getOutlinePaint(): TextPaint {
        if (outlinePaint == null) {
            outlinePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE; color = outlineColor
                strokeWidth = outlineWidth; textSize = this@QuranEntity.textSize
                typeface = this@QuranEntity.typeface; alpha = opacity
            }
        }
        return outlinePaint!!
    }

    fun createLayout(availableWidth: Int) {
        val paint = getTextPaint()
        staticLayout = StaticLayout.Builder.obtain(aya, 0, aya.length, paint, availableWidth)
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

    fun invalidatePaints() { textPaint = null; outlinePaint = null }
    override fun getType(): EntityType = EntityType.QURAN
}
