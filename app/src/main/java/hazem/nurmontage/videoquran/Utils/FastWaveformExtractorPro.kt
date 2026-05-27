package hazem.nurmontage.videoquran.Utils

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import java.nio.ByteBuffer

object FastWaveformExtractorPro {

    @Throws(Exception::class)
    fun extract(filePath: String, targetSamples: Int): FloatArray {
        val extractor = MediaExtractor()
        extractor.setDataSource(filePath)

        var audioTrackIndex = -1
        for (i in 0 until extractor.trackCount) {
            if (extractor.getTrackFormat(i).getString("mime")?.startsWith("audio/") == true) {
                audioTrackIndex = i
                break
            }
        }

        if (audioTrackIndex < 0) {
            extractor.release()
            return FloatArray(targetSamples)
        }

        extractor.selectTrack(audioTrackIndex)
        val trackFormat = extractor.getTrackFormat(audioTrackIndex)

        val codec = MediaCodec.createDecoderByType(trackFormat.getString("mime")!!)
        codec.configure(trackFormat, null, null, 0)
        codec.start()

        val bufferInfo = MediaCodec.BufferInfo()
        val rawAmplitudes = FloatArray(2000)
        var isEos = false
        var rawIndex = 0

        while (!isEos) {
            val inputBufferIndex = codec.dequeueInputBuffer(0L)
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
                val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0L)
                if (outputBufferIndex < 0) break

                val outputBuffer = codec.getOutputBuffer(outputBufferIndex)!!
                if (rawIndex < 2000) {
                    rawAmplitudes[rawIndex] = computeMaxAmp(outputBuffer, bufferInfo.size)
                    rawIndex++
                }
                codec.releaseOutputBuffer(outputBufferIndex, false)

                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    isEos = true
                    break
                }
            }
        }

        codec.stop()
        codec.release()
        extractor.release()

        return downsample(rawAmplitudes, rawIndex, targetSamples)
    }

    private fun computeMaxAmp(buffer: ByteBuffer, size: Int): Float {
        buffer.position(0)
        var max = 0f
        var i = 0
        while (i < size - 1) {
            max = maxOf(max, Math.abs(buffer.getShort(i).toInt()).toFloat())
            i += 2
        }
        return max / 32767.0f
    }

    private fun downsample(raw: FloatArray, rawCount: Int, targetSamples: Int): FloatArray {
        val result = FloatArray(targetSamples)
        val ratio = rawCount.toFloat() / targetSamples

        for (i in 0 until targetSamples) {
            val start = (i * ratio).toInt()
            val end = ((i + 1) * ratio).toInt()
            var peak = 0f
            for (j in start until end.coerceAtMost(rawCount)) {
                peak = maxOf(peak, raw[j])
            }
            result[i] = peak
        }
        return result
    }
}
