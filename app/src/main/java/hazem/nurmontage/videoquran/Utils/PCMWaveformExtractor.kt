package hazem.nurmontage.videoquran.Utils

import java.io.File
import java.io.FileInputStream
import java.io.IOException

object PCMWaveformExtractor {

    @Throws(IOException::class)
    fun extractWaveform(filePath: String, targetSamples: Int): FloatArray {
        val result = FloatArray(targetSamples)
        val inputStream = FileInputStream(filePath)
        val buffer = ByteArray(8192)
        val samplesPerPixel = (File(filePath).length() / 2).toFloat() / targetSamples.toFloat()

        var sampleIndex = 0
        while (true) {
            val bytesRead = inputStream.read(buffer)
            if (bytesRead <= 0) {
                inputStream.close()
                return result
            }
            var i = 0
            while (i < bytesRead) {
                val position = sampleIndex / samplesPerPixel
                if (position >= targetSamples) break

                val bucketIndex = position.toInt()
                val sample = ((buffer[i + 1].toInt() shl 8) or (buffer[i].toInt() and 0xFF)).toShort()
                result[bucketIndex] = maxOf(result[bucketIndex], Math.abs(sample.toInt()) / 32767.0f)
                sampleIndex++
                i += 2
            }
        }
    }
}
