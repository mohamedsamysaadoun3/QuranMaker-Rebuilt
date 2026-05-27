package hazem.nurmontage.videoquran.Utils

import android.media.MediaExtractor
import android.media.MediaFormat
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Extracts audio amplitudes using MediaCodec for waveform display.
 */
object WaveformExtractor {

    fun extractAmplitudes(filePath: String, samplesCount: Int = 200): FloatArray {
        val extractor = MediaExtractor()
        return try {
            extractor.setDataSource(filePath)
            val audioTrack = findAudioTrack(extractor) ?: return FloatArray(0)
            val format = extractor.getTrackFormat(audioTrack)
            extractor.selectTrack(audioTrack)

            val durationUs = format.getLongValue(MediaFormat.KEY_DURATION)
            val sampleSize = durationUs / samplesCount

            val rawAmplitudes = mutableListOf<Float>()
            val buffer = java.nio.ByteBuffer.allocate(1024)

            while (true) {
                val sampleSizeRead = extractor.readSampleData(buffer, 0)
                if (sampleSizeRead <= 0) break

                var sum = 0L
                for (i in 0 until sampleSizeRead step 2) {
                    val sample = buffer.getShort(i).toInt()
                    sum += abs(sample).toLong()
                }
                rawAmplitudes.add(sum.toFloat() / sampleSizeRead)

                extractor.advance()
            }

            normalizeAmplitudes(rawAmplitudes.toFloatArray(), samplesCount)
        } catch (_: Exception) {
            FloatArray(0)
        } finally {
            extractor.release()
        }
    }

    private fun findAudioTrack(extractor: MediaExtractor): Int? {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
            if (mime.startsWith("audio/")) return i
        }
        return null
    }

    private fun normalizeAmplitudes(raw: FloatArray, targetCount: Int): FloatArray {
        if (raw.isEmpty()) return FloatArray(targetCount)

        val maxAmp = raw.maxOrNull() ?: 1f
        if (maxAmp == 0f) return FloatArray(targetCount)

        val result = FloatArray(targetCount)
        val samplesPerBucket = raw.size.toFloat() / targetCount

        for (i in 0 until targetCount) {
            val start = (i * samplesPerBucket).toInt()
            val end = ((i + 1) * samplesPerBucket).toInt().coerceAtMost(raw.size)
            var sum = 0f
            for (j in start until end) {
                sum += raw[j]
            }
            result[i] = if (end > start) (sum / (end - start)) / maxAmp else 0f
        }
        return result
    }

    private fun MediaFormat.getLongValue(key: String): Long {
        return try { getLong(key) } catch (_: Exception) { 0L }
    }
}
