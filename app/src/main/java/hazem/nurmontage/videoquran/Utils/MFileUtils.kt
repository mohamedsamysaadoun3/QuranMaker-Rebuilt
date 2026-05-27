package hazem.nurmontage.videoquran.Utils

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * File utility constants and nested data classes.
 */
object MFileUtils {

    data class FileInfo(
        val name: String,
        val lastModified: Long,
        val formattedDate: String = formatDateShort(lastModified),
        val timedDate: String = if (lastModified > 0)
            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(lastModified))
        else ""
    ) : Serializable

    fun formatDateShort(timestamp: Long): String {
        return try {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }
}
