package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View

/**
 * Custom discrete seekbar with labeled tick marks and gradient progress.
 * Stub implementation – full drawing/interaction logic to be added later.
 */
class CustomDiscreteSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mCurrentProgressIndex: Int = 0
    private var mMaxProgressIndex: Int = -1
    private var mListener: OnProgressChangeListener? = null

    private val mTrackPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mProgressPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mThumbPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mTickPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mTextPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mTrackRect: RectF = RectF()

    interface OnProgressChangeListener {
        fun onProgressChanged(seekBar: CustomDiscreteSeekBar, index: Int, label: String, fromUser: Boolean)
        fun onStartTrackingTouch(seekBar: CustomDiscreteSeekBar)
        fun onStopTrackingTouch(seekBar: CustomDiscreteSeekBar)
    }

    fun setOnProgressChangeListener(listener: OnProgressChangeListener?) {
        mListener = listener
    }

    fun setProgress(index: Int) {
        if (index < 0 || index > mMaxProgressIndex) return
        mCurrentProgressIndex = index
        invalidate()
    }

    fun getProgress(): Int = mCurrentProgressIndex

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Stub: full seekbar drawing logic to be implemented
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
