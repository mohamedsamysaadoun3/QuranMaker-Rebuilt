package hazem.nurmontage.videoquran.multitouch

import android.content.Context
import android.view.MotionEvent

class RotateGestureDetector(context: Context, private val mListener: OnRotateGestureListener) : TwoFingerGestureDetector(context) {

    interface OnRotateGestureListener {
        fun onRotate(detector: RotateGestureDetector): Boolean
        fun onRotateBegin(detector: RotateGestureDetector): Boolean
        fun onRotateEnd(detector: RotateGestureDetector)
    }

    open class SimpleOnRotateGestureListener : OnRotateGestureListener {
        override fun onRotate(detector: RotateGestureDetector) = false
        override fun onRotateBegin(detector: RotateGestureDetector) = true
        override fun onRotateEnd(detector: RotateGestureDetector) {}
    }

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
                    mGestureInProgress = mListener.onRotateBegin(this)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (mSloppyGesture) {
                    mSloppyGesture = isSloppyGesture(event)
                    if (!mSloppyGesture) {
                        mGestureInProgress = mListener.onRotateBegin(this)
                    }
                }
            }
        }
    }

    override fun handleInProgressEvent(action: Int, event: MotionEvent) {
        when (action) {
            MotionEvent.ACTION_MOVE -> {
                updateStateByEvent(event)
                if (mCurrPressure / mPrevPressure > PRESSURE_THRESHOLD && mListener.onRotate(this)) {
                    mPrevEvent?.recycle()
                    mPrevEvent = MotionEvent.obtain(event)
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                if (!mSloppyGesture) mListener.onRotateEnd(this)
                resetState()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                updateStateByEvent(event)
                if (!mSloppyGesture) mListener.onRotateEnd(this)
                resetState()
            }
        }
    }

    override fun resetState() {
        super.resetState()
        mSloppyGesture = false
    }

    fun getRotationDegreesDelta(): Float {
        return Math.toDegrees(
            Math.atan2(mPrevFingerDiffY.toDouble(), mPrevFingerDiffX.toDouble()) -
            Math.atan2(mCurrFingerDiffY.toDouble(), mCurrFingerDiffX.toDouble())
        ).toFloat()
    }
}
