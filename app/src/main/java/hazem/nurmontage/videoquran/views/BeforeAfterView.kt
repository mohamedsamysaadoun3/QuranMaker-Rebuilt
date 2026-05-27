package hazem.nurmontage.videoquran.views

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.FutureTarget
import com.bumptech.glide.signature.ObjectKey
import hazem.nurmontage.videoquran.Utils.AppUtils
import hazem.nurmontage.videoquran.Utils.LocaleHelper
import hazem.nurmontage.videoquran.Utils.ScreenUtils
import hazem.nurmontage.videoquran.common.Common
import java.util.concurrent.ExecutionException

class BeforeAfterView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var dividerX: Float = -1f
        private set
    private var beforeImage: Bitmap? = null
    private var afterImage: Bitmap? = null
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFF0000.toInt() }
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFF0000.toInt()
        style = Paint.Style.FILL
    }
    private val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var circleRadius = 0f
    private var hintAnimator: ValueAnimator? = null
    private var isStartAnim = false
    private var isShowTxt = false
    private var txt: String? = null
    private var textPaint: Paint? = null
    private var xText = 0f
    private var yText = 0f

    fun setTxt(text: String) { txt = text }

    fun showText(size: Int) {
        isShowTxt = true
        textPaint = Paint().apply {
            typeface = Typeface.createFromAsset(resources.assets, "fonts/arabic/فرشة.ttf")
            textSize = calculateTextSize(txt!!, size, this)
        }
    }

    private fun calculateTextSize(text: String, maxSize: Int, paint: Paint): Float {
        var size = 400f
        paint.textSize = size
        val rect = Rect()
        while (rect.width() > maxSize || rect.height() > maxSize) {
            size -= 1f
            paint.textSize = size
            paint.getTextBounds(text, 0, text.length, rect)
        }
        val half = maxSize / 2f
        xText = half - rect.width() / 2f
        yText = half + rect.height() / 2f
        return size
    }

    init {
        ScreenUtils.getScreenWidth(context)
        Thread {
            try {
                val before = beforeImage
                val after = afterImage
                if (before != null && after != null) {
                    addTextPaint(before, after, context)
                    invalidate()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun addTextPaint(before: Bitmap, after: Bitmap, context: Context) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = before.width * 0.025f
            typeface = Typeface.createFromAsset(resources.assets, "fonts/arabic/${Common.english_app_font}")
        }
        val canvas = Canvas()
        val leftMargin = before.width * 0.04f
        val topMargin = before.height * 0.025f

        if (LocaleHelper.getLanguage(context) == "ar") {
            paint.color = -7829368
            canvas.setBitmap(before)
            canvas.drawText("قبل", leftMargin, topMargin, paint)
            canvas.setBitmap(after)
            val rightX = (before.width - paint.measureText("بعد")) - leftMargin
            paint.color = -15605
            canvas.drawText("بعد", rightX, topMargin, paint)
        } else {
            paint.color = -7829368
            canvas.setBitmap(before)
            canvas.drawText("BEFORE", leftMargin, topMargin, paint)
            canvas.setBitmap(after)
            val rightX = (before.width - paint.measureText("AFTER")) - leftMargin
            paint.color = -15605
            canvas.drawText("AFTER", rightX, topMargin, paint)
        }
    }

    private fun initHintAnimation(size: Int) {
        if (hintAnimator?.isRunning == true) return
        val start = dividerX
        val animator = ValueAnimator.ofFloat(start, start + size * 0.065f)
        hintAnimator = animator
        animator.duration = 700L
        animator.repeatMode = ValueAnimator.REVERSE
        animator.repeatCount = ValueAnimator.INFINITE
        animator.addUpdateListener { anim ->
            dividerX = anim.animatedValue as Float
            invalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(size, size)
        dividerX = size / 2f
        circleRadius = size * 0.05f
        linePaint.strokeWidth = circleRadius * 0.1f
        initHintAnimation(size)
    }

    fun release() {
        beforeImage?.let { if (!it.isRecycled) it.recycle() }
        afterImage?.let { if (!it.isRecycled) it.recycle() }
    }

    fun setBeforeImage(bitmap: Bitmap) {
        beforeImage?.let { if (!it.isRecycled) it.recycle() }
        beforeImage = bitmap
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(ViewCompat.MEASURED_STATE_MASK)

        if (isShowTxt && txt != null && textPaint != null) {
            canvas.drawColor(-1)
            canvas.save()
            canvas.clipRect(dividerX, 0f, width.toFloat(), height.toFloat())
            canvas.drawText(txt!!, xText, yText, textPaint!!)
            canvas.restore()
            canvas.drawLine(dividerX, 0f, dividerX, height.toFloat(), linePaint)
            canvas.drawCircle(dividerX, height / 2f, circleRadius, circlePaint)
            drawArrows(canvas, dividerX, height / 2f)
        } else {
            val before = beforeImage
            val after = afterImage
            if (before != null && after != null) {
                canvas.drawBitmap(before, 0f, 0f, imagePaint)
                canvas.save()
                canvas.clipRect(dividerX, 0f, width.toFloat(), height.toFloat())
                canvas.drawBitmap(after, 0f, 0f, imagePaint)
                canvas.restore()
                canvas.drawLine(dividerX, 0f, dividerX, height.toFloat(), linePaint)
                canvas.drawCircle(dividerX, height / 2f, circleRadius, circlePaint)
                drawArrows(canvas, dividerX, height / 2f)
            }
        }

        if (!isStartAnim) {
            hintAnimator?.start()
            isStartAnim = true
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isStartAnim && event.action == MotionEvent.ACTION_DOWN) {
            hintAnimator?.cancel()
        }
        if (event.action == MotionEvent.ACTION_MOVE) {
            dividerX = event.x
            invalidate()
        }
        return true
    }

    private fun drawArrows(canvas: Canvas, cx: Float, cy: Float) {
        val arrowSize = circleRadius / 3f
        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = -1
            style = Paint.Style.FILL
        }

        // Left arrow
        val leftPath = Path().apply {
            moveTo(cx - circleRadius / 2f, cy)
            lineTo(cx - circleRadius / 2f + arrowSize, cy - arrowSize)
            lineTo(cx - circleRadius / 2f + arrowSize, cy + arrowSize)
            close()
        }
        canvas.drawPath(leftPath, whitePaint)

        // Right arrow
        val rightPath = Path().apply {
            moveTo(cx + circleRadius / 2f, cy)
            lineTo(cx + circleRadius / 2f - arrowSize, cy - arrowSize)
            lineTo(cx + circleRadius / 2f - arrowSize, cy + arrowSize)
            close()
        }
        canvas.drawPath(rightPath, whitePaint)
    }

    companion object {
        @Throws(ExecutionException::class, InterruptedException::class)
        fun get(context: Context, w: Int, h: Int, resId: Int): Bitmap {
            val target: FutureTarget<Bitmap> = Glide.with(context)
                .asBitmap()
                .load(resId)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .signature(ObjectKey(AppUtils.getVersionName(context)))
                .override(w, h)
                .centerInside()
                .submit()
            val bitmap = target.get().copy(Bitmap.Config.ARGB_8888, true)
            Glide.with(context).clear(target)
            return bitmap
        }

        fun getActivity(context: Context): Activity? {
            return if (context is ContextWrapper && context is Activity) context else null
        }
    }
}
