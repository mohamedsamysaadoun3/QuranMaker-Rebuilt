package hazem.nurmontage.videoquran.Utils

/**
 * Time formatting utility.
 */
object TimeFormatter {

    fun formatMs(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun formatMsDetailed(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val millis = (ms % 1000) / 10
        return String.format("%02d:%02d.%02d", minutes, seconds, millis)
    }

    fun formatHms(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun parseToMs(timeStr: String): Long {
        val parts = timeStr.split(":")
        return when (parts.size) {
            2 -> (parts[0].toLongOrNull() ?: 0L) * 60 * 1000 + (parts[1].toLongOrNull() ?: 0L) * 1000
            3 -> (parts[0].toLongOrNull() ?: 0L) * 3600 * 1000 + (parts[1].toLongOrNull() ?: 0L) * 60 * 1000 + (parts[2].toLongOrNull() ?: 0L) * 1000
            else -> 0L
        }
    }
}
