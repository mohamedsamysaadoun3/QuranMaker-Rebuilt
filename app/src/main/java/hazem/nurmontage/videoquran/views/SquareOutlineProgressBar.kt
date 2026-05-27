package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View

/**
 * Square outline progress bar with gradient stroke and text overlay.
 * Stub implementation – full drawing logic to be added later.
 */
class SquareOutlineProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val trackPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val progressPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val textPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val rect: RectF = RectF()
    private val path: Path = Path()
    private val partialPath: Path = Path()
    private val pathMeasure: PathMeasure = PathMeasure()

    var progressValue: Int = 0
        private set
    var maxProgress: Int = 100
        private set

    init {
        init()
    }

    private fun init() {
        textPaint.color = -1 // white
        try {
            textPaint.typeface = Typeface.createFromAsset(resources.assets, "fonts/ReadexPro_Medium.ttf")
        } catch (_: Exception) {
            // Font asset not found – stub mode, silently skip
        }
    }

    fun setProgress(value: Int) {
        val clamped = value.coerceIn(0, maxProgress)
        if (progressValue != clamped) {
            progressValue = clamped
            invalidate()
        }
    }

    fun getProgress(): Int = progressValue

    fun setMaxProgress(max: Int) {
        maxProgress = max.coerceAtLeast(1)
        invalidate()
    }

    fun setCornerRadius(radius: Float) {
        invalidate()
    }

    fun setStrokeWidth(width: Float) {
        trackPaint.strokeWidth = width
        progressPaint.strokeWidth = width
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension((size / 1.618034f).toInt(), size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Stub: full progress bar drawing logic to be implemented
    }
}
