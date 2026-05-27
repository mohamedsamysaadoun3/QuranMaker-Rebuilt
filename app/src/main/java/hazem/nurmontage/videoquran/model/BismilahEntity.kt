package hazem.nurmontage.videoquran.model

import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import hazem.nurmontage.videoquran.constant.AyaTextPreset
import java.io.Serializable

class BismilahEntity : EntityView() {

    var text: String = "\u0628\u0633\u0645 \u0627\u0644\u0644\u0647 \u0627\u0644\u0631\u062D\u0645\u0646 \u0627\u0644\u0631\u062D\u064A\u0645"
    var fontName: String = "\u062E\u0637 \u0627\u0644\u0625\u0628\u0644.otf"
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
        set(value) {
            field = value
            invalidatePaints()
        }
    var alignment: Layout.Alignment = Layout.Alignment.ALIGN_CENTER
    var lineSpacing: Float = 1.2f
    var fadeInDuration: Long = 0L
    var fadeOutDuration: Long = 0L
    var animationProgress: Float = 1f
    private var textPaint: TextPaint? = null
    var staticLayout: StaticLayout? = null
    private var bismilahTimeline: BismilahTimeline = BismilahTimeline()

    fun getTextPaint(): TextPaint {
        if (textPaint == null) {
            textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor; textSize = this@BismilahEntity.textSize
                typeface = this@BismilahEntity.typeface; alpha = opacity
            }
        }
        return textPaint!!
    }

    fun getPaintAya(): TextPaint = getTextPaint()

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

    override fun draw(canvas: Canvas) {
        val layout = staticLayout ?: return
        canvas.save()
        canvas.translate(rectF.left, rectF.top)
        layout.draw(canvas)
        canvas.restore()
    }

    fun invalidatePaints() { textPaint = null }
    override fun getType(): EntityType = EntityType.BISMILAH

    // === BlurredImageView integration methods ===

    fun getBismilahTimeline(): BismilahTimeline = bismilahTimeline

    fun setColor(color: Int) {
        textColor = color
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

    /**
     * Timeline visibility data for bismilah entities.
     */
    class BismilahTimeline : Serializable {
        var startTime: Long = 0L
        var endTime: Long = 0L
        var visible: Boolean = true

        fun visible(): Boolean = visible
    }
}
