package hazem.nurmontage.videoquran.model

import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import hazem.nurmontage.videoquran.constant.SurahNameStyle
import java.io.Serializable

class SurahNameEntity : EntityView(), Serializable {

    var surahName: String = ""
    var fontName: String = "خط الإبل.otf"
    var typeface: Typeface? = null
    var textColor: Int = Color.WHITE
    var zaghrafatColor: Int = Color.parseColor("#FFD700")
    var bgColor: Int = Color.TRANSPARENT
    var textSize: Float = 22f
    var opacity: Int = 255
    var style: SurahNameStyle = SurahNameStyle.NONE
    var alignment: Layout.Alignment = Layout.Alignment.ALIGN_CENTER
    var lineSpacing: Float = 1.0f
    var surahNumber: Int = 0
    private var textPaint: TextPaint? = null
    private var zaghrafatPaint: Paint? = null
    var staticLayout: StaticLayout? = null

    fun getTextPaint(): TextPaint {
        if (textPaint == null) {
            textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor; textSize = this@SurahNameEntity.textSize
                typeface = this@SurahNameEntity.typeface; alpha = opacity
                textAlign = Paint.Align.CENTER
            }
        }
        return textPaint!!
    }

    fun createLayout(availableWidth: Int) {
        val paint = getTextPaint()
        staticLayout = StaticLayout.Builder.obtain(surahName, 0, surahName.length, paint, availableWidth)
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

    fun invalidatePaints() { textPaint = null; zaghrafatPaint = null }
    override fun getType(): EntityType = EntityType.SURAH_NAME
}
