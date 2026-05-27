package hazem.nurmontage.videoquran.Utils

import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AmplitudeExtract {

    companion object {
        private const val TAG = "AudioAmplitudeReader"
    }

    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    interface AmplitudeDataCallback {
        fun onComplete(amplitudes: List<Float>)
        fun onError(exception: Exception)
    }

    fun extractAmplitudeDataAsync(filePath: String, targetSamples: Int, callback: AmplitudeDataCallback) {
        executorService.execute {
            try {
                val result = extractAmplitudeData(filePath, targetSamples)
                mainHandler.post { callback.onComplete(result) }
            } catch (e: Exception) {
                Log.e(TAG, "Error extracting amplitude data", e)
                mainHandler.post { callback.onError(e) }
            }
        }
    }

    @Throws(IOException::class, IllegalArgumentException::class)
    private fun extractAmplitudeData(filePath: String, targetSamples: Int): List<Float> {
        require(filePath.isNotEmpty()) { "File path cannot be null or empty." }
        require(targetSamples > 0) { "Target samples must be greater than zero." }

        val result = mutableListOf<Float>()
        var extractor: MediaExtractor? = null

        try {
            extractor = MediaExtractor()
            extractor.setDataSource(filePath)

            var audioTrackIndex = -1
            var audioFormat: MediaFormat? = null

            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString("mime")
                if (mime != null && mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    audioFormat = format
                    break
                }
            }

            if (audioTrackIndex == -1 || audioFormat == null) {
                throw IOException("No audio track found in $filePath")
            }

            extractor.selectTrack(audioTrackIndex)

            if (audioFormat.getLong("durationUs") <= 0) {
                Log.w(TAG, "Duration not available or invalid, results might be inaccurate for downsampling.")
            }

            val buffer = ByteBuffer.allocate(16384)
            buffer.order(ByteOrder.nativeOrder())

            val rawSamples = mutableListOf<Short>()
            while (true) {
                val readSize = extractor.readSampleData(buffer, 0)
                if (readSize < 0) break

                if (readSize > 0) {
                    buffer.position(0)
                    buffer.limit(readSize)
                    val shortBuffer = buffer.asShortBuffer()
                    while (shortBuffer.hasRemaining()) {
                        rawSamples.add(shortBuffer.get())
                    }
                }
                buffer.clear()
                extractor.advance()
            }

            if (rawSamples.isEmpty()) {
                for (i in 0 until targetSamples) result.add(0f)
                return result
            }

            val totalSamples = rawSamples.size
            val samplesPerBucket = maxOf(1, totalSamples / targetSamples)

            for (i in 0 until targetSamples) {
                val start = i * samplesPerBucket
                val end = minOf(start + samplesPerBucket, totalSamples)
                if (start >= totalSamples) {
                    result.add(0f)
                } else {
                    var peak: Short = 0
                    for (j in start until end) {
                        val sample = rawSamples[j]
                        if (Math.abs(sample.toInt()) > Math.abs(peak.toInt())) {
                            peak = sample
                        }
                    }
                    result.add(Math.abs(peak.toInt()) / 32767.0f)
                }
            }

            return result
        } finally {
            extractor?.release()
        }
    }
}
