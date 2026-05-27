package hazem.nurmontage.videoquran.Utils

import android.os.SystemClock
import android.view.Choreographer

class SmoothTimelineAnimator(
    private val startCursorMs: Int,
    private val maxTimeMs: Int,
    private val listener: AnimatorListener
) {

    interface AnimatorListener {
        fun onUpdate(timeMs: Int)
        fun onEnd()
    }

    private var isRunning = false
    private var currentTimeMs = startCursorMs
    private var startTimeMs = 0L

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (isRunning) {
                val elapsed = (SystemClock.uptimeMillis() - startTimeMs).toInt()
                currentTimeMs = startCursorMs + elapsed
                if (currentTimeMs >= maxTimeMs) {
                    listener.onUpdate(maxTimeMs)
                    listener.onEnd()
                    isRunning = false
                } else {
                    listener.onUpdate(currentTimeMs)
                    Choreographer.getInstance().postFrameCallback(this)
                }
            }
        }
    }

    fun isRunning(): Boolean = isRunning

    fun getCurrentTimeMs(): Int = currentTimeMs

    fun start() {
        isRunning = true
        startTimeMs = SystemClock.uptimeMillis()
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    fun stop() {
        isRunning = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }
}
