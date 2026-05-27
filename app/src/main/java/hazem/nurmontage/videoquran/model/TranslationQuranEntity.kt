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
        set(value) {
            field = value
            when (value) {
                AyaTextPreset.OUTLINE -> {
                    outlineWidth = 2f
                    outlineColor = Color.BLACK
                }
                AyaTextPreset.SHADOW -> {
                    shadowRadius = 4f
                    shadowColor = Color.BLACK
                }
                AyaTextPreset.GLOW -> {
                    // glow effect
                }
                else -> { /* NONE */ }
            }
            invalidatePaints()
        }

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

    fun getPaintAya(): TextPaint = getTextPaint()

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

    override fun draw(canvas: Canvas) {
        val layout = staticLayout ?: return
        canvas.save()
        canvas.translate(rectF.left, rectF.top)
        layout.draw(canvas)
        canvas.restore()
    }

    fun invalidatePaints() { textPaint = null }
    override fun getType(): EntityType = EntityType.TRANSLATION

    // === BlurredImageView integration methods ===

    fun setNameFont(name: String) { fontName = name }
    fun getNameFont(): String = fontName

    fun setTypeface(tf: Typeface, name: String) {
        typeface = tf
        fontName = name
        invalidatePaints()
    }

    fun setColor(color: Int) {
        textColor = color
        colorTrsl = color
        invalidatePaints()
    }

    fun updateSize(canvasWidth: Int, ayaRect: RectF) {
        rectF.left = ayaRect.left
        rectF.top = ayaRect.top
        rectF.right = ayaRect.right
        rectF.bottom = ayaRect.bottom
        createLayout(ayaRect.width().toInt())
    }

    fun updateSizeResize(canvasWidth: Int, ayaRect: RectF) {
        updateSize(canvasWidth, ayaRect)
    }

    fun applyAll(canvasWidth: Int, rect: RectF, textSize: Float, factorSize: Float) {
        this.textSize = textSize
        this.factorSize = factorSize
        rectF.left = rect.left
        rectF.top = rect.top
        rectF.right = rect.right
        rectF.bottom = rect.bottom
        invalidatePaints()
        createLayout(rect.width().toInt())
    }
}
