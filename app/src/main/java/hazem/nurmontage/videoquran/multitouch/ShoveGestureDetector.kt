package hazem.nurmontage.videoquran.multitouch

import android.content.Context
import android.view.MotionEvent

class ShoveGestureDetector(context: Context, private val mListener: OnShoveGestureListener) : TwoFingerGestureDetector(context) {

    interface OnShoveGestureListener {
        fun onShove(detector: ShoveGestureDetector): Boolean
        fun onShoveBegin(detector: ShoveGestureDetector): Boolean
        fun onShoveEnd(detector: ShoveGestureDetector)
    }

    open class SimpleOnShoveGestureListener : OnShoveGestureListener {
        override fun onShove(detector: ShoveGestureDetector) = false
        override fun onShoveBegin(detector: ShoveGestureDetector) = true
        override fun onShoveEnd(detector: ShoveGestureDetector) {}
    }

    private var mCurrAverageY = 0f
    private var mPrevAverageY = 0f
    private var mSloppyGesture = false

    override fun handleStartProgressEvent(action: Int, event: MotionEvent) {
        when (action) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                resetState()
                mPrevEvent = MotionEvent.obtain(event)
                mTimeDelta = 0L
                updateStateByEvent(event)
                mSloppyGesture = isSloppyGesture(event)
                if (!mSloppyGesture) {
                    mGestureInProgress = mListener.onShoveBegin(this)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (mSloppyGesture) {
                    mSloppyGesture = isSloppyGesture(event)
                    if (!mSloppyGesture) {
                        mGestureInProgress = mListener.onShoveBegin(this)
                    }
                }
            }
        }
    }

    override fun handleInProgressEvent(action: Int, event: MotionEvent) {
        when (action) {
            MotionEvent.ACTION_MOVE -> {
                updateStateByEvent(event)
                if (mCurrPressure / mPrevPressure > PRESSURE_THRESHOLD &&
                    Math.abs(getShovePixelsDelta()) > 0.5f && mListener.onShove(this)
                ) {
                    mPrevEvent?.recycle()
                    mPrevEvent = MotionEvent.obtain(event)
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                if (!mSloppyGesture) mListener.onShoveEnd(this)
                resetState()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                updateStateByEvent(event)
                if (!mSloppyGesture) mListener.onShoveEnd(this)
                resetState()
            }
        }
    }

    override fun updateStateByEvent(event: MotionEvent) {
        super.updateStateByEvent(event)
        val prev = mPrevEvent ?: return
        mPrevAverageY = (prev.getY(0) + prev.getY(1)) / 2f
        mCurrAverageY = (event.getY(0) + event.getY(1)) / 2f
    }

    override fun isSloppyGesture(event: MotionEvent): Boolean {
        if (super.isSloppyGesture(event)) return true
        val angle = Math.abs(Math.atan2(mCurrFingerDiffY.toDouble(), mCurrFingerDiffX.toDouble()))
        return (angle > 0.0 && angle < 0.35) || (angle > 2.79 && angle < Math.PI)
    }

    fun getShovePixelsDelta(): Float = mCurrAverageY - mPrevAverageY

    override fun resetState() {
        super.resetState()
        mSloppyGesture = false
        mPrevAverageY = 0f
        mCurrAverageY = 0f
    }
}
