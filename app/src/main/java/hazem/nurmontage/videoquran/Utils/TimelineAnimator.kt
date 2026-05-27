package hazem.nurmontage.videoquran.Utils

import android.os.Handler

class TimelineAnimator(
    startTimeMs: Int,
    private val maxTimeMs: Int,
    private val listener: AnimatorListener
) {

    interface AnimatorListener {
        fun onUpdate(timeMs: Int)
        fun onEnd()
    }

    private var currentTimeMs = startTimeMs
    private var startTimeMs = startTimeMs
    private var isRunning = false
    private var lastFrameTime = 0L
    private val handler = Handler()

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                val now = System.currentTimeMillis()
                val delta = (now - lastFrameTime).toInt()
                lastFrameTime = now
                currentTimeMs += delta

                if (currentTimeMs >= maxTimeMs) {
                    currentTimeMs = maxTimeMs
                    listener.onUpdate(currentTimeMs)
                    listener.onEnd()
                    isRunning = false
                    return
                }

                listener.onUpdate(currentTimeMs)
                postFrame()
            }
        }
    }

    fun isRunning(): Boolean = isRunning

    fun getCurrentTimeMs(): Int = currentTimeMs

    fun start() {
        isRunning = true
        lastFrameTime = System.currentTimeMillis()
        postFrame()
    }

    fun stop() {
        isRunning = false
        handler.removeCallbacks(updateRunnable)
    }

    private fun postFrame() {
        handler.postDelayed(updateRunnable, 16L)
    }
}
