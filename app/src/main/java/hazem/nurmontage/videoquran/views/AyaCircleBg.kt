package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class AyaCircleBg @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var typeface: Typeface? = null
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val circleRect = RectF()
    private val circlePath = Path()

    init {
        if (this.typeface == null) {
            val tf = Typeface.createFromAsset(resources.assets, "fonts/arabic/محمدي.ttf")
            this.typeface = tf
            setTypeface(tf)
        }
    }

    override fun onDraw(canvas: Canvas) {
        val text = text.toString()
        val paint: TextPaint = paint
        val textWidth = paint.measureText(text)
        val fontMetrics = paint.fontMetrics
        val maxRadius = Math.max(textWidth, fontMetrics.descent - fontMetrics.ascent) / 2f + 20f
        val cx = width / 2f
        val cy = height / 2f
        val halfText = textWidth / 2f

        bgPaint.shader = LinearGradient(
            cx - halfText, cy, cx + halfText, cy,
            intArrayOf(Color.parseColor("#B7833AB4"), Color.parseColor("#E1306C"), Color.parseColor("#BCF58529")),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, maxRadius, bgPaint)
        super.onDraw(canvas)
    }
}
