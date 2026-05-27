package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.view.ViewCompat

class CassetteView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var labelText: String = "Titanium – David Guetta Ft. Sia"

    private val paintBody = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E74C3C") }
    private val paintShadow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#A93226") }
    private val paintLabel = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FAE5D3") }
    private val paintReel = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ViewCompat.MEASURED_STATE_MASK }
    private val paintHole = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#5DADE2") }
    private val paintAccent = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E67E22") }
    private val paintScrew = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val paintFloor = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#6EC6E9") }
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ViewCompat.MEASURED_STATE_MASK
        textSize = 36f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        canvas.drawColor(Color.parseColor("#87CEEB"))

        // Floor
        val floorPath = Path().apply {
            moveTo(0f, 0.7f * h)
            lineTo(w, 0.7f * h)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        canvas.drawPath(floorPath, paintFloor)

        // Shadow
        val shadowRect = RectF(0.12f * w, 0.32f * h, 0.92f * w, 0.72f * h)
        val left = w * 0.1f
        val top = h * 0.3f
        val right = 0.9f * w
        val bottom = 0.7f * h
        canvas.drawRoundRect(shadowRect, 20f, 20f, paintShadow)

        // Body
        val bodyRect = RectF(left, top, right, bottom)
        canvas.drawRoundRect(bodyRect, 20f, 20f, paintBody)

        // Top accent
        val accentPath = Path().apply {
            moveTo(left, top)
            lineTo(right, top)
            lineTo(w * 0.85f, h * 0.35f)
            lineTo(w * 0.15f, h * 0.35f)
            close()
        }
        canvas.drawPath(accentPath, paintAccent)

        // Bottom accent
        val bottomAccent = Path().apply {
            moveTo(left, bottom)
            lineTo(right, bottom)
            lineTo(w * 0.85f, h * 0.65f)
            lineTo(w * 0.15f, h * 0.65f)
            close()
        }
        canvas.drawPath(bottomAccent, paintAccent)

        // Top label
        val labelLeft = 0.2f * w
        val labelRight = 0.8f * w
        canvas.drawRect(RectF(labelLeft, 0.36f * h, labelRight, 0.44f * h), paintLabel)
        canvas.drawText(labelText, (w - paintText.measureText(labelText)) / 2f, 0.415f * h, paintText)

        // Bottom label
        canvas.drawRect(RectF(labelLeft, 0.48f * h, labelRight, 0.62f * h), paintLabel)

        // Reels
        val reelRadius = h * 0.1f
        val innerR1 = reelRadius * 0.3f
        val innerR2 = reelRadius * 0.45f
        val reelY = h * 0.55f
        val reelX1 = 0.35f * w
        val reelX2 = 0.65f * w

        canvas.drawCircle(reelX1, reelY, reelRadius, paintReel)
        drawInnerGear(canvas, reelX1, reelY, innerR1, innerR2, 8, paintHole)
        canvas.drawCircle(reelX2, reelY, reelRadius, paintReel)
        drawInnerGear(canvas, reelX2, reelY, innerR1, innerR2, 8, paintHole)

        // Screws
        val screwRadius = w * 0.015f
        val x1 = w * 0.15f
        val x2 = w * 0.85f
        canvas.drawCircle(x1, h * 0.34f, screwRadius, paintScrew)
        canvas.drawCircle(x2, h * 0.34f, screwRadius, paintScrew)
        canvas.drawCircle(x1, h * 0.66f, screwRadius, paintScrew)
        canvas.drawCircle(x2, h * 0.66f, screwRadius, paintScrew)
    }

    private fun drawInnerGear(canvas: Canvas, cx: Float, cy: Float, r1: Float, r2: Float, teeth: Int, paint: Paint) {
        val path = Path()
        val totalTeeth = teeth * 2
        val angleStep = 2.0 * Math.PI / totalTeeth

        for (i in 0 until totalTeeth) {
            val angle = i * angleStep
            val radius = if (i % 2 == 0) r1 else r2
            val x = (cx + Math.cos(angle) * radius).toFloat()
            val y = (cy + radius * Math.sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, paint)
    }
}
