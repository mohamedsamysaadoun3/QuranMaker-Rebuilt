package hazem.nurmontage.videoquran.Utils

import android.content.Context
import android.media.MediaScannerConnection
import java.io.File

/**
 * MediaScanner wrapper for new files.
 * Makes newly created files visible in gallery/file managers.
 */
object FileMediaScanner {

    fun scanFile(context: Context, file: File) {
        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)
    }

    fun scanFile(context: Context, path: String) {
        MediaScannerConnection.scanFile(context, arrayOf(path), null, null)
    }

    fun scanDirectory(context: Context, directory: File) {
        val files = directory.listFiles() ?: return
        val paths = files.map { it.absolutePath }.toTypedArray()
        MediaScannerConnection.scanFile(context, paths, null, null)
    }
}
