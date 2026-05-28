package hazem.nurmontage.videoquran

import android.graphics.Bitmap
import android.net.Uri
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.io.File
import java.util.concurrent.Executor

class EngineBackgroundManager(
    private val activity: EngineActivity,
    private val executor: Executor
) {
    fun setupOriginalBitmap(uri: Uri, maxWidth: Int, maxHeight: Int, onResult: (Bitmap?) -> Unit) {
        executor.execute {
            try {
                val bitmap = Glide.with(activity)
                    .asBitmap()
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .submit(maxWidth, maxHeight)
                    .get()
                activity.runOnUiThread { onResult(bitmap) }
            } catch (e: Exception) {
                e.printStackTrace()
                activity.runOnUiThread { onResult(null) }
            }
        }
    }

    fun extractAudioFromVideo(videoPath: String, onResult: (String?) -> Unit) {
        val output = File(activity.cacheDir, "audio_extract_${System.currentTimeMillis()}.mp3")
        val cmd = "-y -i \"$videoPath\" -vn -acodec copy -y \"${output.absolutePath}\""

        FFmpegKit.executeAsync(cmd, { session ->
            if (ReturnCode.isSuccess(session.returnCode) && output.exists()) {
                activity.runOnUiThread { onResult(output.absolutePath) }
            } else {
                activity.runOnUiThread { onResult(null) }
            }
        })
    }

    fun extractVideoFrames(videoPath: String, fps: Int, onFirstFrame: (String?) -> Unit) {
        val framesDir = File(activity.cacheDir, "frames")
        framesDir.mkdirs()
        framesDir.listFiles()?.forEach { it.delete() }

        val cmd = "-y -i \"$videoPath\" -vf fps=$fps -q:v 2 \"${framesDir.absolutePath}/frame_%04d.jpg\""

        FFmpegKit.executeAsync(cmd, { session ->
            if (ReturnCode.isSuccess(session.returnCode)) {
                val firstFrame = File(framesDir, "frame_0001.jpg")
                activity.runOnUiThread { onFirstFrame(if (firstFrame.exists()) firstFrame.absolutePath else null) }
            } else {
                activity.runOnUiThread { onFirstFrame(null) }
            }
        })
    }
}
