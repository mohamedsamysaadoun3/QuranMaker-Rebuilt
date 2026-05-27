package hazem.nurmontage.videoquran.multitouch

import android.content.Context
import android.graphics.PointF
import android.view.MotionEvent

class MoveGestureDetector(context: Context, private val mListener: OnMoveGestureListener) : BaseGestureDetector(context) {

    interface OnMoveGestureListener {
        fun onMove(detector: MoveGestureDetector): Boolean
        fun onMoveBegin(detector: MoveGestureDetector): Boolean
        fun onMoveEnd(detector: MoveGestureDetector)
    }

    open class SimpleOnMoveGestureListener : OnMoveGestureListener {
        override fun onMove(detector: MoveGestureDetector) = false
        override fun onMoveBegin(detector: MoveGestureDetector) = true
        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    private var mCurrFocusInternal: PointF = PointF()
    private var mPrevFocusInternal: PointF = PointF()
    private var mFocusDeltaExternal: PointF = PointF()
    private val mFocusExternal = PointF()

    override fun handleStartProgressEvent(action: Int, event: MotionEvent) {
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                resetState()
                mPrevEvent = MotionEvent.obtain(event)
                mTimeDelta = 0L
                updateStateByEvent(event)
            }
            MotionEvent.ACTION_MOVE -> {
                mGestureInProgress = mListener.onMoveBegin(this)
            }
        }
    }

    override fun handleInProgressEvent(action: Int, event: MotionEvent) {
        when (action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mListener.onMoveEnd(this)
                resetState()
            }
            MotionEvent.ACTION_MOVE -> {
                updateStateByEvent(event)
                if (mCurrPressure / mPrevPressure > PRESSURE_THRESHOLD && mListener.onMove(this)) {
                    mPrevEvent?.recycle()
                    mPrevEvent = MotionEvent.obtain(event)
                }
            }
        }
    }

    override fun updateStateByEvent(event: MotionEvent) {
        super.updateStateByEvent(event)
        val prev = mPrevEvent ?: return
        mCurrFocusInternal = determineFocalPoint(event)
        mPrevFocusInternal = determineFocalPoint(prev)
        mFocusDeltaExternal = if (prev.pointerCount != event.pointerCount) {
            PointF(0f, 0f)
        } else {
            PointF(mCurrFocusInternal.x - mPrevFocusInternal.x, mCurrFocusInternal.y - mPrevFocusInternal.y)
        }
        mFocusExternal.x += mFocusDeltaExternal.x
        mFocusExternal.y += mFocusDeltaExternal.y
    }

    private fun determineFocalPoint(event: MotionEvent): PointF {
        var x = 0f
        var y = 0f
        for (i in 0 until event.pointerCount) {
            x += event.getX(i)
            y += event.getY(i)
        }
        val count = event.pointerCount.toFloat()
        return PointF(x / count, y / count)
    }

    fun getFocusX(): Float = mFocusExternal.x
    fun getFocusY(): Float = mFocusExternal.y
    fun getFocusDelta(): PointF = mFocusDeltaExternal
}
