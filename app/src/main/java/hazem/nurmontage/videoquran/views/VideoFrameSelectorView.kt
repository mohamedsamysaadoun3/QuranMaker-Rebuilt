package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class VideoFrameSelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface OnFrameSelectedListener {
        fun onFrameSelected(index: Int, bitmap: Bitmap)
    }

    class BitmapFrame(val bitmap: Bitmap, val time: Long)

    var onFrameSelectedListener: OnFrameSelectedListener? = null

    private var videoUri: Uri? = null
    private var frameCount = 7
    private val frameBitmaps = mutableListOf<BitmapFrame>()
    private var selectedFrameIndex = 0

    private val framePaint = Paint().apply { color = -7829368 }
    private val cursorPaint = Paint().apply {
        color = -65536 // CATEGORY_MASK equivalent
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }
    private val frameRect = RectF()
    private var frameWidth = 0f
    private var frameHeight = 0f
    private var frameSpacing = 1f
    private var cornerRadius = 10f
    private var cursorX = 0f

    fun setVideoUri(uri: Uri) {
        videoUri = uri
        loadFrames()
        invalidate()
    }

    private fun loadFrames() {
        val uri = videoUri ?: return
        frameBitmaps.clear()
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: return
            val interval = duration / frameCount
            for (i in 0 until frameCount) {
                val timeUs = i * interval * 1000
                val frame = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                if (frame != null) {
                    frameBitmaps.add(BitmapFrame(frame, timeUs))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        if (frameCount > 0) {
            frameWidth = w.toFloat() / frameCount
            frameHeight = frameWidth
            cursorX = frameWidth / 2f
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (frameBitmaps.isEmpty()) {
            canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), cornerRadius, cornerRadius, framePaint)
            return
        }
        canvas.save()
        canvas.translate(0f, (height - frameHeight) * 0.5f)
        for (i in frameBitmaps.indices) {
            val left = i * (frameSpacing + frameWidth)
            frameRect.set(left, 0f, frameWidth + left, frameHeight)
            canvas.drawRoundRect(frameRect, cornerRadius, cornerRadius, framePaint)
            val bmp = frameBitmaps[i].bitmap
            if (bmp != null) {
                canvas.drawBitmap(bmp, Rect(0, 0, bmp.width, bmp.height), frameRect, null)
            }
        }
        canvas.restore()
        canvas.drawLine(cursorX, 0f, cursorX, height.toFloat(), cursorPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            val x = event.x.coerceIn(0f, width.toFloat())
            cursorX = x
            var index = (x / (frameWidth + frameSpacing)).toInt()
            index = index.coerceIn(0, frameCount - 1)
            selectedFrameIndex = index
            invalidate()
            return true
        }
        return super.onTouchEvent(event)
    }

    fun getFrameBitmap(): BitmapFrame? {
        if (selectedFrameIndex < 0 || selectedFrameIndex >= frameBitmaps.size) return null
        return frameBitmaps[selectedFrameIndex]
    }
}
