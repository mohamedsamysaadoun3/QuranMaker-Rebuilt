package hazem.nurmontage.videoquran.Utils

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import java.nio.ByteBuffer

object FastWaveformExtractor {

    @Throws(Exception::class)
    fun extract(filePath: String, samples: Int): FloatArray {
        val result = FloatArray(samples)
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
            return result
        }

        extractor.selectTrack(audioTrackIndex)
        val trackFormat = extractor.getTrackFormat(audioTrackIndex)

        val codec = MediaCodec.createDecoderByType(trackFormat.getString("mime")!!)
        codec.configure(trackFormat, null, null, 0)
        codec.start()

        val durationPerSample = trackFormat.getLong("durationUs") / samples
        val inputBuffers = codec.inputBuffers
        val outputBuffers = codec.outputBuffers
        val bufferInfo = MediaCodec.BufferInfo()

        var currentSampleTime = 0L
        var sampleIndex = 0

        while (sampleIndex < samples) {
            extractor.seekTo(currentSampleTime, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            val nextSampleTime = currentSampleTime + durationPerSample

            val inputBufferIndex = codec.dequeueInputBuffer(5000L)
            if (inputBufferIndex >= 0) {
                val readSize = extractor.readSampleData(inputBuffers[inputBufferIndex], 0)
                if (readSize < 0) break
                codec.queueInputBuffer(inputBufferIndex, 0, readSize, extractor.sampleTime, 0)
                extractor.advance()
            }

            val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 5000L)
            if (outputBufferIndex >= 0) {
                result[sampleIndex] = computeAmp(outputBuffers[outputBufferIndex], bufferInfo.size)
                sampleIndex++
                codec.releaseOutputBuffer(outputBufferIndex, false)
            }

            currentSampleTime = nextSampleTime
        }

        codec.stop()
        codec.release()
        extractor.release()
        return result
    }

    private fun computeAmp(buffer: ByteBuffer, size: Int): Float {
        buffer.position(0)
        var max = 0f
        var i = 0
        while (i < size - 1) {
            max = maxOf(max, Math.abs(buffer.getShort(i).toInt()) / 32767.0f)
            i += 2
        }
        return max
    }
}
