package hazem.nurmontage.videoquran.Utils

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import java.nio.ByteBuffer
import java.nio.ByteOrder

object FastWaveform {

    fun decodeWaveform(context: Context, uri: Uri, samples: Int): FloatArray {
        val result = FloatArray(samples)
        var extractor: MediaExtractor? = null
        var codec: MediaCodec? = null

        try {
            extractor = MediaExtractor()
            extractor.setDataSource(context, uri, null)

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

            if (audioTrackIndex < 0 || audioFormat == null) return result

            extractor.selectTrack(audioTrackIndex)

            codec = MediaCodec.createDecoderByType(audioFormat.getString("mime")!!)
            codec.configure(audioFormat, null, null, 0)
            codec.start()

            val samplesPerBucket = (audioFormat.getLong("durationUs") / 1000000 *
                    audioFormat.getInteger("sample-rate")) / samples

            val bufferInfo = MediaCodec.BufferInfo()
            var isEos = false
            var sampleIndex = 0
            var peak = 0f
            var sampleCount = 0L

            while (!isEos) {
                val inputBufferIndex = codec.dequeueInputBuffer(10000L)
                if (inputBufferIndex >= 0) {
                    val inputBuffer = codec.getInputBuffer(inputBufferIndex)!!
                    val readSize = extractor.readSampleData(inputBuffer, 0)
                    if (readSize < 0) {
                        codec.queueInputBuffer(inputBufferIndex, 0, 0, 0L,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    } else {
                        codec.queueInputBuffer(inputBufferIndex, 0, readSize,
                            extractor.sampleTime, 0)
                        extractor.advance()
                    }
                }

                val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 10000L)
                if (outputBufferIndex >= 0) {
                    val outputBuffer = codec.getOutputBuffer(outputBufferIndex)!!
                    outputBuffer.order(ByteOrder.LITTLE_ENDIAN)

                    while (outputBuffer.remaining() > 1) {
                        val amp = Math.abs(outputBuffer.short.toInt()) / 32768.0f
                        if (amp > peak) peak = amp
                        sampleCount++
                        if (sampleCount >= samplesPerBucket) {
                            if (sampleIndex < samples) {
                                result[sampleIndex] = peak
                            }
                            sampleIndex++
                            if (sampleIndex >= samples) {
                                isEos = true
                                break
                            }
                            peak = 0f
                            sampleCount = 0
                        }
                    }
                    codec.releaseOutputBuffer(outputBufferIndex, false)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        isEos = true
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try { codec?.stop() } catch (_: Exception) {}
            try { codec?.release() } catch (_: Exception) {}
            try { extractor?.release() } catch (_: Exception) {}
        }

        return result
    }
}
