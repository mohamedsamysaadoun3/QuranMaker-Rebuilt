package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.common.Common

class SquareImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintRect = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var isSelect = false
    private var number: String? = null
    private var anInt = 0
    private var cx = 0f
    private var cy = 0f
    private var r = 0f
    private var x = 0f
    private var y = 0f
    private var drawableDone: Drawable? = null

    fun getAnInt(): Int = anInt

    fun setNumber(i: Int) {
        if (i == 0) return
        anInt = i
        number = i.toString()
        cx = (width * 0.5f) - (textPaint.measureText(number!!) * 0.5f)
    }

    fun isMSelect(): Boolean = isSelect

    init {
        textPaint.color = -1
        textPaint.typeface = Typeface.createFromAsset(resources.assets, "fonts/${Common.english_app_font}")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val w = measuredWidth
        setMeasuredDimension(w, w)
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        paintRect.color = -1056964608 // 0xC0000000
        paint.strokeWidth = 0.02f * w

        if (!isSelect) {
            paint.color = -8355712
            paint.style = Paint.Style.STROKE
        } else {
            paint.color = -12190534
            paint.style = Paint.Style.FILL
        }

        textPaint.textSize = 0.25f * w
        r = 0.1f * w
        x = w - 1.2f * r
        y = r + paint.strokeWidth

        if (number != null) {
            cx = (width * 0.5f) - (textPaint.measureText(number!!) * 0.5f)
        }
        cy = height * 0.5f

        val iconSize = (w * 0.3f).toInt()
        val centerX = (width * 0.5f).toInt()
        val checkRect = android.graphics.Rect(
            centerX - iconSize, (cy - iconSize).toInt(),
            centerX + iconSize, (cy + iconSize).toInt()
        )
        val drawable = ContextCompat.getDrawable(context, R.drawable.check_24px)
        this.drawableDone = drawable
        drawable?.bounds = checkRect
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isSelect) return

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paintRect)
        drawableDone?.draw(canvas)
        number?.let { canvas.drawText(it, cx, cy, textPaint) }
    }

    fun onSelect(select: Boolean) {
        isSelect = select
        if (!select) {
            paint.color = -8355712
            paint.style = Paint.Style.STROKE
        } else {
            paint.color = -12190534
            paint.style = Paint.Style.FILL
        }
        invalidate()
    }
}
