package hazem.nurmontage.videoquran.Utils

import android.content.Context
import android.graphics.*
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur

/**
 * Bitmap utility - cropping, scaling, blurring, round corners.
 * Used throughout the app for image processing.
 */
object UtilsBitmap {

    fun cropBitmap(source: Bitmap, x: Int, y: Int, width: Int, height: Int): Bitmap {
        return Bitmap.createBitmap(source, x, y, width, height)
    }

    fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val ratio = Math.min(
            maxWidth.toFloat() / bitmap.width,
            maxHeight.toFloat() / bitmap.height
        )
        val width = Math.round(bitmap.width * ratio)
        val height = Math.round(bitmap.height * ratio)
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    fun scaleBitmapToWidth(bitmap: Bitmap, newWidth: Int): Bitmap {
        val ratio = newWidth.toFloat() / bitmap.width
        val newHeight = Math.round(bitmap.height * ratio)
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    fun blurBitmap(context: Context, bitmap: Bitmap, radius: Float = 25f): Bitmap {
        val rs = RenderScript.create(context)
        val input = Allocation.createFromBitmap(rs, bitmap)
        val output = Allocation.createTyped(rs, input.type)
        val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        script.setRadius(radius.coerceIn(0f, 25f))
        script.setInput(input)
        script.forEach(output)
        output.copyTo(bitmap)
        rs.destroy()
        return bitmap
    }

    fun getRoundedCornerBitmap(bitmap: Bitmap, pixels: Int): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)
        val roundPx = pixels.toFloat()
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    fun getCircleBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle(
            bitmap.width / 2f, bitmap.height / 2f,
            Math.min(bitmap.width, bitmap.height) / 2f, paint
        )
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    fun createBlurredBackground(context: Context, source: Bitmap, viewWidth: Int, viewHeight: Int): Bitmap {
        val scaled = scaleBitmap(source, viewWidth / 4, viewHeight / 4)
        return try {
            blurBitmap(context, scaled, 25f)
        } catch (e: Exception) {
            scaleBitmap(source, viewWidth, viewHeight)
        }
    }

    fun addOverlay(bitmap: Bitmap, overlayColor: Int, alpha: Int = 128): Bitmap {
        val output = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)
        val paint = Paint().apply {
            color = overlayColor
            this.alpha = alpha
        }
        canvas.drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paint)
        return output
    }
}
