package hazem.nurmontage.videoquran.views

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.FutureTarget
import com.bumptech.glide.signature.ObjectKey
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.Utils.AppUtils
import hazem.nurmontage.videoquran.Utils.LocaleHelper
import hazem.nurmontage.videoquran.Utils.ScreenUtils
import java.util.concurrent.ExecutionException

class CropViewHint @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val linePaint = Paint().apply {
        color = 0xFFFF0000.toInt()
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    private val arrowPaint = Paint().apply {
        color = 0xFFFF0000.toInt()
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val cropPaint = Paint().apply {
        color = -15605
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }
    private val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = -1 }
    private val mTextRect = Rect()
    private var mTittle: String
    private var bitmap: Bitmap? = null
    private var ipadBitmap: Bitmap? = null
    private var cropRect: RectF? = null
    private var radius = 0f
    private var arrowHeadSize = 0
    private var endX = 0f
    private var endY = 0f
    private var endLineX = 0f
    private var endLineY = 0f
    private var endLineXArrow = 0f
    private var endLineYArrow = 0f
    private var xText = 0f
    private var yBitmap = 0f
    private var yText = 0f

    init {
        if (LocaleHelper.getLanguage(context) == "ar") {
            mTittle = "تحكم في شاشة الآيبود"
            textPaint.typeface = Typeface.createFromAsset(resources.assets, "fonts/ReadexPro_Medium.ttf")
        } else {
            mTittle = "iPod screen selection"
            textPaint.typeface = Typeface.createFromAsset(resources.assets, "fonts/ReadexPro_Medium.ttf")
        }

        val screenWidth = (ScreenUtils.getScreenWidth(context) * 0.52f).toInt()
        Thread {
            try {
                bitmap = get(context, screenWidth, screenWidth, R.drawable.bg_13)
                invalidate()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(size, size)
        linePaint.strokeWidth = 0.0085f * size
        textPaint.textSize = size * 0.045f
        textPaint.getTextBounds(mTittle, 0, mTittle.length, mTextRect)
        xText = (size - mTextRect.width()) * 0.5f
        yText = mTextRect.height() * 1.2f
        yBitmap = yText + mTextRect.height()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(ViewCompat.MEASURED_STATE_MASK)

        val bmp = bitmap ?: return
        canvas.drawBitmap(bmp, 0f, yBitmap, imagePaint)
        canvas.drawText(mTittle, xText, yText, textPaint)

        val ipad = ipadBitmap ?: return
        if (cropRect == null) {
            val offsetX = bmp.width * 0.25f
            val offsetY = bmp.height * 0.08f + yBitmap
            cropRect = RectF(offsetX, offsetY, offsetX + bmp.width * 0.35f, offsetY + bmp.height * 0.43f)
            radius = Math.min(cropRect!!.width(), cropRect!!.height()) * 0.108f
            arrowHeadSize = (bmp.width * 0.1f).toInt()
            endX = width - ipad.width.toFloat()
            endY = yBitmap + bmp.height.toFloat()
            val h2 = ipad.height * 0.28f
            val w2 = ipad.width * 0.3f
            endLineYArrow = endY + h2
            endLineXArrow = endX + w2
            endLineY = endY * 0.98f + h2
            endLineX = endX * 0.98f + w2
        }

        canvas.drawRoundRect(cropRect!!, radius, radius, cropPaint)
        canvas.drawBitmap(ipad, endX, endY, imagePaint)
        canvas.drawLine(cropRect!!.centerX(), cropRect!!.centerY(), endLineX, endLineY, linePaint)
        drawArrowHead(canvas, endLineXArrow, endLineYArrow, 0f, 0f)
    }

    private fun drawArrowHead(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float) {
        val angle = Math.atan2((y1 - y2).toDouble(), (x1 - x2).toDouble())
        val x3 = (x1 - arrowHeadSize * Math.cos(angle - 0.5236)).toFloat()
        val y3 = (y1 - arrowHeadSize * Math.sin(angle - 0.5236)).toFloat()
        val x4 = (x1 - arrowHeadSize * Math.cos(angle + 0.5236)).toFloat()
        val y4 = (y1 - arrowHeadSize * Math.sin(angle + 0.5236)).toFloat()
        val path = Path().apply {
            moveTo(x1, y1)
            lineTo(x3, y3)
            lineTo(x4, y4)
            close()
        }
        canvas.drawPath(path, arrowPaint)
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
