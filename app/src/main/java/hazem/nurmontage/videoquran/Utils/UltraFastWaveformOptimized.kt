package hazem.nurmontage.videoquran.Utils

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import java.io.IOException
import java.nio.ByteBuffer

object UltraFastWaveformOptimized {

    @Throws(IOException::class)
    fun extractAmplitudes(filePath: String, targetSamples: Int): FloatArray {
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

        val bufferInfo = MediaCodec.BufferInfo()
        val result = FloatArray(targetSamples)
        val sampleCounts = IntArray(targetSamples)
        audioFormat.getLong("durationUs")

        var isEos = false
        var globalSampleIndex = 0

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
                    val sampleCount = bufferInfo.size / 2
                    val ratio = sampleCount.toFloat() / targetSamples

                    var i = 0
                    while (i < sampleCount) {
                        val sample = outputBuffer.getShort(i * 2)
                        val bucketIndex = (globalSampleIndex / ratio).toInt()
                        if (bucketIndex >= targetSamples) break
                        result[bucketIndex] = maxOf(
                            result[bucketIndex],
                            Math.abs(sample.toInt()) / 32767.0f
                        )
                        globalSampleIndex++
                        i += 2
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
        return result
    }
}
