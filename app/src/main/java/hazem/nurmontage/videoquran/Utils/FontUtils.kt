package hazem.nurmontage.videoquran.Utils

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Copies font from assets to internal storage.
 */
object FontUtils {

    fun copyFontToInternal(context: Context, assetPath: String): File {
        val fileName = assetPath.substringAfterLast("/")
        val outFile = File(context.filesDir, "fonts/$fileName")
        if (outFile.exists()) return outFile

        outFile.parentFile?.mkdirs()
        context.assets.open(assetPath).use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        }
        return outFile
    }

    fun getFontFile(context: Context, fontName: String): File? {
        val internalFile = File(context.filesDir, "fonts/$fontName")
        if (internalFile.exists()) return internalFile
        return null
    }
}
