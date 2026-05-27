package hazem.nurmontage.videoquran.Utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object AudioUploadHelper {

    private const val TAG = "AudioUploadHelper"

    fun processAudioUriForUpload(context: Context, uri: Uri, fileName: String): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) return null

            val outputDir = File(context.cacheDir, "audio_upload").apply {
                if (!exists()) mkdirs()
            }
            val outputFile = File(outputDir, fileName)

            FileOutputStream(outputFile).use { outputStream ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
            }
            inputStream.close()

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
