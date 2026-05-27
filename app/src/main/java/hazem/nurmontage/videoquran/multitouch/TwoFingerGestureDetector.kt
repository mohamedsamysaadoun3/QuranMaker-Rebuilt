package hazem.nurmontage.videoquran.multitouch

import android.content.Context
import android.view.MotionEvent

abstract class TwoFingerGestureDetector(context: Context) : BaseGestureDetector(context) {

    protected var mCurrFingerDiffX = 0f
    protected var mCurrFingerDiffY = 0f
    protected var mPrevFingerDiffX = 0f
    protected var mPrevFingerDiffY = 0f
    private var mCurrLen = -1f
    private var mPrevLen = -1f
    private val mEdgeSlop: Float = android.view.ViewConfiguration.get(context).scaledEdgeSlop.toFloat()
    private var mRightSlopEdge = 0f
    private var mBottomSlopEdge = 0f

    override fun updateStateByEvent(event: MotionEvent) {
        super.updateStateByEvent(event)
        val prev = mPrevEvent ?: return
        mCurrLen = -1f
        mPrevLen = -1f

        val prevX0 = prev.getX(0)
        val prevY0 = prev.getY(0)
        val prevX1 = prev.getX(1)
        val prevY1 = prev.getY(1)
        mPrevFingerDiffX = prevX1 - prevX0
        mPrevFingerDiffY = prevY1 - prevY0

        val currX0 = event.getX(0)
        val currY0 = event.getY(0)
        val currX1 = event.getX(1)
        val currY1 = event.getY(1)
        mCurrFingerDiffX = currX1 - currX0
        mCurrFingerDiffY = currY1 - currY0
    }

    fun getCurrentSpan(): Float {
        if (mCurrLen == -1f) {
            val x = mCurrFingerDiffX
            val y = mCurrFingerDiffY
            mCurrLen = Math.sqrt((x * x + y * y).toDouble()).toFloat()
        }
        return mCurrLen
    }

    fun getPreviousSpan(): Float {
        if (mPrevLen == -1f) {
            val x = mPrevFingerDiffX
            val y = mPrevFingerDiffY
            mPrevLen = Math.sqrt((x * x + y * y).toDouble()).toFloat()
        }
        return mPrevLen
    }

    protected open fun isSloppyGesture(event: MotionEvent): Boolean {
        val metrics = mContext.resources.displayMetrics
        mRightSlopEdge = metrics.widthPixels - mEdgeSlop
        mBottomSlopEdge = metrics.heightPixels - mEdgeSlop

        val rawX = event.rawX
        val rawY = event.rawY
        val rawX2 = getRawX(event, 1)
        val rawY2 = getRawY(event, 1)

        val isSloppy1 = rawX < mEdgeSlop || rawY < mEdgeSlop || rawX > mRightSlopEdge || rawY > mBottomSlopEdge
        val isSloppy2 = rawX2 < mEdgeSlop || rawY2 < mEdgeSlop || rawX2 > mRightSlopEdge || rawY2 > mBottomSlopEdge

        return (isSloppy1 && isSloppy2) || isSloppy1 || isSloppy2
    }

    companion object {
        protected fun getRawX(event: MotionEvent, pointerIndex: Int): Float {
            val offset = event.x - event.rawX
            return if (pointerIndex < event.pointerCount) event.getX(pointerIndex) + offset else 0f
        }

        protected fun getRawY(event: MotionEvent, pointerIndex: Int): Float {
            val offset = event.y - event.rawY
            return if (pointerIndex < event.pointerCount) event.getY(pointerIndex) + offset else 0f
        }
    }
}
