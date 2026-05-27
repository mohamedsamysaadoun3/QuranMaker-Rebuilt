package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface OnWaveformClickListener {
        fun onProgressChanged(progress: Float)
    }

    var listener: OnWaveformClickListener? = null

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private var amplitudes: IntArray = intArrayOf(30, 40, 60, 80, 50, 90, 100, 70, 40, 60, 80, 50, 30, 50, 70, 90, 60, 40)

    private var progress: Float = 0f

    fun setProgress(p: Float) {
        progress = p
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            var x = event.x / width
            if (x < 0f) x = 0f
            if (x > 1f) x = 1f
            setProgress(x)
            listener?.onProgressChanged(x)
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val barWidth = w / (amplitudes.size * 2)

        for (i in amplitudes.indices) {
            val barHeight = (amplitudes[i] / 100f) * h
            val left = i.toFloat() * (barWidth + barWidth)
            val top = (h - barHeight) / 2f
            val barProgress = i.toFloat() / amplitudes.size

            paint.color = if (progress > 0f && barProgress < progress) {
                -1 // White
            } else {
                -12303292 // Gray
            }

            canvas.drawRoundRect(left, top, left + barWidth, top + barHeight, 5f, 5f, paint)
        }
    }
}
