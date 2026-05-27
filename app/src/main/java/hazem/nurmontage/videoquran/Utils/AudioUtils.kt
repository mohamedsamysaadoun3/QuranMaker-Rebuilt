package hazem.nurmontage.videoquran.Utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

/**
 * Async audio file download/copy utility.
 */
object AudioUtils {

    suspend fun downloadAudio(url: String, outputFile: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            URL(url).openStream().use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
            }
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun copyAudioToInternal(context: Context, sourcePath: String, fileName: String): File? {
        return try {
            val outFile = File(context.filesDir, "audio/$fileName")
            outFile.parentFile?.mkdirs()
            File(sourcePath).inputStream().use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
            outFile
        } catch (_: Exception) {
            null
        }
    }

    fun getAudioDuration(filePath: String): Long {
        return try {
            val extractor = android.media.MediaExtractor()
            extractor.setDataSource(filePath)
            val format = extractor.getTrackFormat(0)
            val duration = format.getLong(android.media.MediaFormat.KEY_DURATION)
            extractor.release()
            duration / 1000 // Convert to milliseconds
        } catch (_: Exception) {
            0L
        }
    }
}
