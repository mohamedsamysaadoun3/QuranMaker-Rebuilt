package hazem.nurmontage.videoquran.views

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.Utils.LocaleHelper
import kotlin.math.abs

class CustomDiscreteSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface OnProgressChangeListener {
        fun onProgressChanged(seekBar: CustomDiscreteSeekBar, index: Int, label: String, fromUser: Boolean)
        fun onStartTrackingTouch(seekBar: CustomDiscreteSeekBar)
        fun onStopTrackingTouch(seekBar: CustomDiscreteSeekBar)
    }

    var mListener: OnProgressChangeListener? = null

    private val mIsRTL: Boolean = LocaleHelper.getLanguage(context) == "ar"
    private var mGradientColors: IntArray = if (mIsRTL) {
        intArrayOf(Color.parseColor("#fae065"), Color.parseColor("#cbd653"), Color.parseColor("#a8ce46"))
    } else {
        intArrayOf(Color.parseColor("#a8ce46"), Color.parseColor("#cbd653"), Color.parseColor("#fae065"))
    }

    private var mTrackHeight = dpToPx(1.2f)
    private var mThumbRadius = dpToPx(10f)
    private var mTickRadius = dpToPx(4f)
    private var mLabelTextSize = spToPx(10.5f)
    private var mPaddingBottom = dpToPx(8f)

    private val mTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = -3355444
        style = Paint.Style.FILL
    }
    private val mProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val mThumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = -3355444
        style = Paint.Style.FILL
    }
    private val mTickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = -3355444
        style = Paint.Style.FILL
    }
    private val mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = -3355444
        textSize = mLabelTextSize
        textAlign = Paint.Align.CENTER
        typeface = Typeface.createFromAsset(resources.assets, "fonts/ReadexPro_Medium.ttf")
    }

    private var mLabels: MutableList<String>
    private var mMaxProgressIndex: Int
    private var mCurrentProgressIndex = 0
    private val mTrackRect = RectF()
    private var mTickPositionsX: FloatArray
    private var mIsDragging = false
    private var mThumbX = 0f

    init {
        var labelsResId = 0
        if (attrs != null) {
            val ta: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomDiscreteSeekBar)
            try {
                labelsResId = ta.getResourceId(R.styleable.CustomDiscreteSeekBar_labelsArray, 0)
            } finally {
                ta.recycle()
            }
        }
        mLabels = if (labelsResId != 0) {
            ArrayList(resources.getStringArray(labelsResId).toList())
        } else {
            ArrayList()
        }
        mMaxProgressIndex = mLabels.size - 1
        mTickPositionsX = FloatArray(mLabels.size)
        calculateThumbPositionForIndex()
    }

    fun getmLabels(): List<String> = mLabels

    private fun dpToPx(dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

    private fun spToPx(sp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)

    fun setProgress(index: Int) {
        if (index < 0 || index > mMaxProgressIndex) return
        val changed = mCurrentProgressIndex != index
        mCurrentProgressIndex = index
        calculateThumbPositionForIndex()
        invalidate()
        if (changed) {
            mListener?.onProgressChanged(this, mCurrentProgressIndex, mLabels[mCurrentProgressIndex], false)
        }
    }

    fun getProgress(): Int = mCurrentProgressIndex

    fun getCurrentLabel(): String {
        if (mCurrentProgressIndex in mLabels.indices) return mLabels[mCurrentProgressIndex]
        return ""
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var desiredW = dpToPx(200f).toInt()
        var desiredH = (mThumbRadius * 2 + mLabelTextSize + mPaddingBottom + dpToPx(8f)).toInt()

        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        if (wMode == MeasureSpec.EXACTLY) desiredW = wSize
        else if (wMode == MeasureSpec.AT_MOST) desiredW = minOf(desiredW, wSize)

        if (hMode == MeasureSpec.EXACTLY) desiredH = hSize
        else if (hMode == MeasureSpec.AT_MOST) desiredH = minOf(desiredH, hSize)

        setMeasuredDimension(desiredW, desiredH)
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        val start = if (mIsRTL) (width - paddingEnd) - mThumbRadius else paddingLeft + mThumbRadius
        val end = if (mIsRTL) paddingLeft + mThumbRadius else (width - paddingEnd) - mThumbRadius

        val left = minOf(start, end)
        val top = paddingTop + mThumbRadius - mTrackHeight / 2f
        val right = maxOf(start, end)
        val bottom = paddingTop + mThumbRadius + mTrackHeight / 2f
        mTrackRect.set(left, top, right, bottom)

        mProgressPaint.shader = LinearGradient(
            mTrackRect.left, mTrackRect.centerY(),
            mTrackRect.right, mTrackRect.centerY(),
            mGradientColors, null, Shader.TileMode.CLAMP
        )

        if (mLabels.size > 1) {
            val step = abs(end - start) / (mLabels.size - 1)
            for (i in mLabels.indices) {
                mTickPositionsX[i] = if (mIsRTL) start - i * step else start + i * step
            }
        } else if (mLabels.size == 1) {
            mTickPositionsX[0] = mTrackRect.centerX()
        }
        calculateThumbPositionForIndex()
    }

    private fun calculateThumbPositionForIndex() {
        mThumbX = if (mMaxProgressIndex >= 0) mTickPositionsX[mCurrentProgressIndex]
        else if (mIsRTL) mTrackRect.right else mTrackRect.left
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Track
        canvas.drawRoundRect(mTrackRect, 100f, 100f, mTrackPaint)
        // Progress
        if (mIsRTL) {
            canvas.drawRoundRect(mThumbX, mTrackRect.top, mTrackRect.right, mTrackRect.bottom, 100f, 100f, mProgressPaint)
        } else {
            canvas.drawRoundRect(mTrackRect.left, mTrackRect.top, mThumbX, mTrackRect.bottom, 100f, 100f, mProgressPaint)
        }
        // Labels
        for (i in mLabels.indices) {
            var labelX = mTickPositionsX[i]
            if (i == 0) {
                labelX += if (mIsRTL) -mThumbRadius * 0.7f else mThumbRadius * 0.7f
            }
            if (i == mLabels.size - 1) {
                labelX += if (mIsRTL) mThumbRadius else -mThumbRadius
            }
            val label = mLabels[i]
            val bounds = Rect()
            mTextPaint.getTextBounds(label, 0, label.length, bounds)
            canvas.drawText(label, labelX, mTrackRect.centerY() + mThumbRadius + mPaddingBottom + bounds.height(), mTextPaint)
        }
        // Thumb
        canvas.drawCircle(mThumbX, mTrackRect.centerY(), mThumbRadius, mThumbPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled || mMaxProgressIndex < 0) return false

        val x = event.x
        val trackLeft = mTrackRect.left
        val trackRight = mTrackRect.right

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!isTouchNearThumbOrTrack(x, event.y)) return super.onTouchEvent(event)
                mIsDragging = true
                mListener?.onStartTrackingTouch(this)
                mThumbX = x.coerceIn(trackLeft, trackRight)
                invalidate()
                performClick()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!mIsDragging) return super.onTouchEvent(event)
                mThumbX = x.coerceIn(trackLeft, trackRight)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!mIsDragging) return super.onTouchEvent(event)
                mIsDragging = false
                val oldIndex = mCurrentProgressIndex
                snapToNearestTickAndNotify(x)
                if (mCurrentProgressIndex != oldIndex) {
                    mListener?.onProgressChanged(this, mCurrentProgressIndex, mLabels[mCurrentProgressIndex], true)
                }
                mListener?.onStopTrackingTouch(this)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun isTouchNearThumbOrTrack(x: Float, y: Float): Boolean {
        val tolerance = dpToPx(20f)
        return y > mTrackRect.centerY() - mThumbRadius - tolerance &&
                y < mTrackRect.centerY() + mThumbRadius + mLabelTextSize + mPaddingBottom + tolerance &&
                x > mTrackRect.left - mThumbRadius - tolerance &&
                x < mTrackRect.right + mThumbRadius + tolerance
    }

    private fun snapToNearestTickAndNotify(x: Float) {
        var bestIndex = 0
        var bestDist = Float.MAX_VALUE
        for (i in mTickPositionsX.indices) {
            val dist = abs(x - mTickPositionsX[i])
            if (dist < bestDist) {
                bestDist = dist
                bestIndex = i
            }
        }
        mCurrentProgressIndex = bestIndex
        calculateThumbPositionForIndex()
        invalidate()
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
