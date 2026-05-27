package hazem.nurmontage.videoquran.multitouch

import android.content.Context
import android.view.MotionEvent

abstract class BaseGestureDetector(protected val mContext: Context) {

    companion object {
        protected const val PRESSURE_THRESHOLD = 0.67f
    }

    protected var mPrevEvent: MotionEvent? = null
    protected var mCurrEvent: MotionEvent? = null
    protected var mGestureInProgress = false
    protected var mTimeDelta: Long = 0
    protected var mCurrPressure = 0f
    protected var mPrevPressure = 0f

    protected abstract fun handleInProgressEvent(action: Int, event: MotionEvent)
    protected abstract fun handleStartProgressEvent(action: Int, event: MotionEvent)

    fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action and MotionEvent.ACTION_MASK
        if (!mGestureInProgress) {
            handleStartProgressEvent(action, event)
        } else {
            handleInProgressEvent(action, event)
        }
        return true
    }

    protected open fun updateStateByEvent(event: MotionEvent) {
        val prev = mPrevEvent
        mCurrEvent?.recycle()
        mCurrEvent = MotionEvent.obtain(event)
        mTimeDelta = event.eventTime - (prev?.eventTime ?: 0L)
        mCurrPressure = event.getPressure(event.actionIndex)
        mPrevPressure = prev?.getPressure(prev.actionIndex) ?: 0f
    }

    protected open fun resetState() {
        mPrevEvent?.recycle()
        mPrevEvent = null
        mCurrEvent?.recycle()
        mCurrEvent = null
        mGestureInProgress = false
    }

    fun isInProgress(): Boolean = mGestureInProgress
    fun getTimeDelta(): Long = mTimeDelta
    fun getEventTime(): Long = mCurrEvent?.eventTime ?: 0L
}
