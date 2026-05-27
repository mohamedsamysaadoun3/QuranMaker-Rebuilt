package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Audio waveform display View.
 * Stub implementation – full drawing/interaction logic to be added later.
 */
class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress: Float = 0f
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    var listener: OnWaveformClickListener? = null

    interface OnWaveformClickListener {
        fun onProgressChanged(progress: Float)
    }

    fun setProgress(progress: Float) {
        this.progress = progress
        invalidate()
    }

    fun setOnWaveformClickListener(listener: OnWaveformClickListener?) {
        this.listener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            var x = event.x / width
            x = x.coerceIn(0f, 1f)
            setProgress(x)
            listener?.onProgressChanged(x)
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Stub: full waveform drawing logic to be implemented
    }
}
