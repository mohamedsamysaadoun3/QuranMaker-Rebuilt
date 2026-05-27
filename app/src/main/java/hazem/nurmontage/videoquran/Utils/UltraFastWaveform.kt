package hazem.nurmontage.videoquran.Utils

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import java.io.IOException
import java.nio.ByteBuffer

object UltraFastWaveform {

    @Throws(IOException::class)
    fun extractAmplitudes(filePath: String, targetSamples: Int): FloatArray {
        val pcmData = decodeToPCM(filePath)
        val length = pcmData.size
        val result = FloatArray(targetSamples)
        val ratio = length.toDouble() / targetSamples

        for (i in 0 until targetSamples) {
            val start = (i * ratio).toInt()
            val end = minOf(((i + 1) * ratio).toInt(), length)
            var peak = 0f
            for (j in start until end) {
                peak = maxOf(peak, Math.abs(pcmData[j].toInt()) / 32767.0f)
            }
            result[i] = peak
        }
        return result
    }

    @Throws(IOException::class)
    private fun decodeToPCM(filePath: String): ShortArray {
        val extractor = MediaExtractor()
        extractor.setDataSource(filePath)

        var audioTrackIndex = -1
        var audioFormat: MediaFormat? = null

        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            if (format.getString("mime")?.startsWith("audio/") == true) {
                audioTrackIndex = i
                audioFormat = format
                break
            }
        }

        if (audioTrackIndex == -1) {
            extractor.release()
            throw IOException("No audio track found")
        }

        extractor.selectTrack(audioTrackIndex)

        val codec = MediaCodec.createDecoderByType(audioFormat!!.getString("mime")!!)
        codec.configure(audioFormat, null, null, 0)
        codec.start()

        val samples = mutableListOf<Short>()
        val bufferInfo = MediaCodec.BufferInfo()
        var isEos = false

        while (!isEos) {
            val inputBufferIndex = codec.dequeueInputBuffer(1000L)
            if (inputBufferIndex >= 0) {
                val inputBuffer = codec.getInputBuffer(inputBufferIndex)!!
                val readSize = extractor.readSampleData(inputBuffer, 0)
                if (readSize < 0) {
                    codec.queueInputBuffer(inputBufferIndex, 0, 0, 0L,
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    isEos = true
                } else {
                    codec.queueInputBuffer(inputBufferIndex, 0, readSize,
                        extractor.sampleTime, 0)
                    extractor.advance()
                }
            }

            while (true) {
                val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 1000L)
                if (outputBufferIndex >= 0) {
                    val outputBuffer = codec.getOutputBuffer(outputBufferIndex)!!
                    outputBuffer.position(0)
                    while (outputBuffer.remaining() > 1) {
                        samples.add(outputBuffer.short)
                    }
                    codec.releaseOutputBuffer(outputBufferIndex, false)
                } else {
                    break
                }
            }
        }

        codec.stop()
        codec.release()
        extractor.release()

        return samples.toShortArray()
    }

    private fun MutableList<Short>.toShortArray(): ShortArray {
        val result = ShortArray(this.size)
        for (i in this.indices) {
            result[i] = this[i]
        }
        return result
    }
}
