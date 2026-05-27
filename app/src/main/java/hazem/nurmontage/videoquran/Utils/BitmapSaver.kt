package hazem.nurmontage.videoquran.Utils

import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

/**
 * Save bitmaps to external files.
 */
object BitmapSaver {

    fun saveBitmap(bitmap: Bitmap, file: File, quality: Int = 95): Boolean {
        return try {
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, quality, fos)
                fos.flush()
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun saveBitmapJpeg(bitmap: Bitmap, file: File, quality: Int = 90): Boolean {
        return try {
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos)
                fos.flush()
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
