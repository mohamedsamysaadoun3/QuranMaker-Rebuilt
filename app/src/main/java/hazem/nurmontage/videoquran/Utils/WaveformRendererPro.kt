package hazem.nurmontage.videoquran.Utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

object WaveformRendererPro {

    fun drawWave(
        width: Int, height: Int, amplitudes: FloatArray,
        color: Int, barGap: Float, cornerRadius: Float, scale: Float
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            alpha = 100
        }

        val w = width.toFloat()
        val h = height.toFloat()
        val count = (amplitudes.size * scale).toInt()
        if (count < 1) return bitmap

        val barWidth = w / count - barGap
        val actualBarWidth = if (barWidth < 1f) 1f else barWidth

        var maxAmp = 0f
        for (amp in amplitudes) maxAmp = maxOf(maxAmp, amp)
        if (maxAmp < 0.01f) maxAmp = 0.01f

        var x = 0f
        for (i in 0 until count) {
            val ampIndex = (i.toFloat() / count * amplitudes.size).toInt()
            val barHeight = (amplitudes[ampIndex] / maxAmp) * h
            canvas.drawRoundRect(
                RectF(x, canvas.height - barHeight, x + actualBarWidth, canvas.height.toFloat()),
                cornerRadius, cornerRadius, paint
            )
            x += w / count
        }
        return bitmap
    }

    fun drawWaveInRect(
        canvas: Canvas, rect: RectF, amplitudes: FloatArray,
        color: Int, barGap: Float, cornerRadius: Float, scale: Float
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            alpha = 100
        }

        val w = rect.width()
        val h = rect.height() * 0.85f
        val count = (amplitudes.size * scale).toInt()
        if (count < 1) return

        val barWidth = w / count - barGap
        val actualBarWidth = if (barWidth < 1f) 1f else barWidth

        var maxAmp = 0f
        for (amp in amplitudes) maxAmp = maxOf(maxAmp, amp)
        if (maxAmp < 0.01f) maxAmp = 0.01f

        var x = rect.left
        for (i in 0 until count) {
            val ampIndex = (i.toFloat() / count * amplitudes.size).toInt()
            val barHeight = (amplitudes[ampIndex] / maxAmp) * h
            canvas.drawRoundRect(
                RectF(x, rect.bottom - barHeight, x + actualBarWidth, rect.bottom),
                cornerRadius, cornerRadius, paint
            )
            x += w / count
        }
    }

    fun drawWaveProportional(
        canvas: Canvas, rect: RectF, amplitudes: FloatArray,
        color: Int, barGap: Float, cornerRadius: Float,
        scale: Float, scrollOffset: Float, barWidth: Float
    ) {
        if (amplitudes.isEmpty()) return

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            alpha = 100
        }

        val w = rect.width()
        val h = rect.height() * 0.85f
        val step = (barWidth + barGap) * scale
        val startBucket = (scrollOffset / step).toInt().coerceIn(0, amplitudes.size - 1)
        val visibleCount = (w / step).toInt() + 2 + startBucket
        val endBucket = visibleCount.coerceAtMost(amplitudes.size)

        var maxAmp = 0f
        for (amp in amplitudes) if (amp > maxAmp) maxAmp = amp
        if (maxAmp < 0.01f) maxAmp = 0.01f

        var x = rect.left - (scrollOffset % step)
        val visibleBars = endBucket - startBucket
        for (i in 0 until visibleBars) {
            val ampIndex = (i.toFloat() / visibleBars * (amplitudes.size - startBucket)).toInt() + startBucket
            if (ampIndex >= amplitudes.size) return
            canvas.drawRoundRect(
                RectF(x, rect.bottom - (amplitudes[ampIndex] / maxAmp) * h,
                    barWidth * scale + x, rect.bottom),
                cornerRadius, cornerRadius, paint
            )
            x += step
            if (x > rect.right) return
        }
    }

    fun drawWaveInRect(
        canvas: Canvas, rect: RectF, amplitudes: FloatArray,
        color: Int, barGap: Float, cornerRadius: Float,
        scale: Float, barWidth: Float, scrollOffset: Float
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            alpha = 100
        }

        val h = rect.height() * 0.85f
        val scaledBarWidth = barWidth * scale
        val step = (barWidth + barGap) * scale
        var startBucket = (scrollOffset / step).toInt()
        if (startBucket < 0) startBucket = 0
        val visibleCount = (rect.width() / step).toInt() + 2 + startBucket
        val endBucket = visibleCount.coerceAtMost(amplitudes.size)

        var maxAmp = 0f
        for (amp in amplitudes) maxAmp = maxOf(maxAmp, amp)
        if (maxAmp < 0.01f) maxAmp = 0.01f

        var x = rect.left
        var i = startBucket
        while (i < endBucket) {
            canvas.drawRoundRect(
                RectF(x, rect.bottom - (amplitudes[i] / maxAmp) * h,
                    x + scaledBarWidth, rect.bottom),
                cornerRadius, cornerRadius, paint
            )
            x += step
            i++
        }
    }

    fun drawWaveInRect(
        canvas: Canvas, rect: RectF, amplitudes: FloatArray,
        color: Int, barGap: Float, cornerRadius: Float,
        scale: Float, startOffset: Int
    ) {
        if (amplitudes.isEmpty()) return

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            alpha = 100
        }

        val w = rect.width()
        val h = rect.height() * 0.85f
        val count = (amplitudes.size * scale).toInt()
        if (count < 1) return

        val safeOffset = startOffset.coerceIn(0, amplitudes.size - count)
        val barWidth = w / count
        val actualBarWidth = maxOf(1f, barWidth - barGap)

        var maxAmp = 0f
        for (i in safeOffset until safeOffset + count) {
            maxAmp = maxOf(maxAmp, amplitudes[i])
        }
        if (maxAmp < 0.01f) maxAmp = 0.01f

        var x = rect.left
        val tempRect = RectF()
        for (i in 0 until count) {
            val idx = safeOffset + i
            if (idx >= amplitudes.size) return
            tempRect.set(x, rect.bottom - (amplitudes[idx] / maxAmp) * h, x + actualBarWidth, rect.bottom)
            canvas.drawRoundRect(tempRect, cornerRadius, cornerRadius, paint)
            x += barWidth
        }
    }

    fun drawWaveformBottom(
        amplitudes: FloatArray, width: Int, height: Int,
        color: Int, gap: Int, cornerRadius: Float, barWidth: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
        }

        var maxAmp = 0f
        for (amp in amplitudes) maxAmp = maxOf(maxAmp, amp)
        if (maxAmp < 0.01f) maxAmp = 0.01f

        var x = 0
        for (amp in amplitudes) {
            val barHeight = (amp / maxAmp * height).toInt()
            canvas.drawRoundRect(
                RectF(
                    x.toFloat(), (height - barHeight).toFloat(),
                    (x + barWidth).toFloat(), height.toFloat()
                ),
                cornerRadius, cornerRadius, paint
            )
            x += barWidth + gap
        }
        return bitmap
    }
}
