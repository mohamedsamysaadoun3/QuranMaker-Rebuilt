package hazem.nurmontage.videoquran.model

import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.graphics.drawable.VectorDrawable
import hazem.nurmontage.videoquran.constant.AyaTextPreset
import java.io.Serializable

class QuranEntity : EntityView(), Serializable {

    var aya: String = ""
    var completeAya: String = ""
    var translation: String? = null
    var translationComplete: String? = null
    var fontName: String = "\u0639\u062B\u0645\u0627\u0646\u064A.otf"
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
        set(value) {
            field = value
            when (value) {
                AyaTextPreset.OUTLINE -> {
                    outlineWidth = 3f
                    outlineColor = Color.BLACK
                }
                AyaTextPreset.SHADOW -> {
                    shadowRadius = 6f
                    shadowColor = Color.BLACK
                }
                AyaTextPreset.GLOW -> {
                    glowRadius = 10f
                    glowColor = Color.WHITE
                }
                else -> { /* NONE */ }
            }
            invalidatePaints()
        }

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
    private var vectorDrawable: VectorDrawable? = null
    var number: Int = -1

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

    fun getPaintAya(): TextPaint = getTextPaint()

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

    override fun draw(canvas: Canvas) {
        val layout = staticLayout ?: return
        canvas.save()
        canvas.translate(rectF.left, rectF.top)
        layout.draw(canvas)
        canvas.restore()
    }

    fun invalidatePaints() { textPaint = null; outlinePaint = null }
    override fun getType(): EntityType = EntityType.QURAN

    // === BlurredImageView integration methods ===

    fun setNameFont(name: String) { fontName = name }
    fun getNameFont(): String = fontName



    fun setVectorDrawable(drawable: VectorDrawable) {
        vectorDrawable = drawable
    }

    fun updateIconDraw() {
        invalidatePaints()
    }

    fun setTypeface(tf: Typeface, name: String) {
        typeface = tf
        fontName = name
        invalidatePaints()
    }

    /** Color of the aya text — alias used by ColorAyaFragment */
    fun getClrAya(): Int = textColor

    /** Preset ordinal — used by ColorAyaFragment */
    fun getmPreset(): Int = preset.ordinal

    /** Map preset ordinal back to AyaTextPreset */
    fun get(presetIndex: Int): AyaTextPreset =
        AyaTextPreset.entries.getOrElse(presetIndex) { AyaTextPreset.NONE }

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

    // === Java-interop property aliases (used by fragments) ===

    /** Alias for [aya] — original Java field name was 'txt' */
    var txt: String
        get() = aya
        set(value) { aya = value }

    /** Alias for [completeAya] — original Java field name was 'complete_aya' */
    var complete_aya: String
        get() = completeAya
        set(value) { completeAya = value }

    /** Alias for [translationComplete] — original Java field name was 'translation_complete' */
    var translation_complete: String?
        get() = translationComplete
        set(value) { translationComplete = value }

    /** Index of the number portion in the text — used by EditTextFragment */
    var indexNumber: Int = -1

    /** Alias for [startWordIndex] — original Java field name was 'startWord_index' */
    var startWord_index: Int
        get() = startWordIndex
        set(value) { startWordIndex = value }

    /** Alias for [endWordIndex] — original Java field name was 'endWord_index' */
    var endWord_index: Int
        get() = endWordIndex
        set(value) { endWordIndex = value }

    /** Reference to the EntityQuranTimeline that owns this entity */
    var entityQuran: hazem.nurmontage.videoquran.entity_timeline.EntityQuranTimeline? = null

    /** Initialize preset from ordinal index — used by EditTextFragment */
    fun initPreset(presetOrdinal: Int) {
        preset = AyaTextPreset.entries.getOrElse(presetOrdinal) { AyaTextPreset.NONE }
    }

    /** Stop any running animation — used by EffectAyaFragment / EffectBismilahFragment */
    fun endAnimator() {
        animationProgress = 1f
        // NOTE: ValueAnimator cancellation should be added when animation system is integrated
    }

    /** Public getter for number — used by BlurredImageView */

    /** Public setter for number */
}
