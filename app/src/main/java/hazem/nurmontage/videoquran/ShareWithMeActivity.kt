package hazem.nurmontage.videoquran

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback
import com.arthenica.ffmpegkit.ReturnCode
import hazem.nurmontage.videoquran.Utils.AudioUploadHelper
import hazem.nurmontage.videoquran.Utils.LocalPersistence
import hazem.nurmontage.videoquran.common.Common
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ShareWithMeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_with_me)

        setStatusBarColor(-1) // White
        setNavigationBarColor(-1) // White

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = true
        insetsController.isAppearanceLightNavigationBars = true

        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val type = intent.type
        if (type == null) {
            startActivity(Intent(this, WorkUserActivity::class.java))
            finish()
            return
        }

        LocalPersistence.deleteTemplate(this, Common.TEMPLATE_TMP)

        when {
            type.startsWith("image/") -> handleImg(intent)
            type.startsWith("audio/") -> handleAudio(intent)
            type.startsWith("video/") -> handleVideo(intent)
        }
    }

    private fun handleVideo(intent: Intent) {
        val uri = getParcelable(intent, Intent.EXTRA_STREAM, Uri::class.java)
        if (uri != null) {
            processVideo(uri)
        }
    }

    private fun copyVideoToCache(uri: Uri): File {
        val file = File(cacheDir, "temp_video.mp4")
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val fileOutputStream = FileOutputStream(file)
            val buffer = ByteArray(4096)
            while (true) {
                val read = inputStream?.read(buffer) ?: -1
                if (read == -1) break
                fileOutputStream.write(buffer, 0, read)
            }
            inputStream?.close()
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file
    }

    private fun processVideo(uri: Uri) {
        val videoPath = copyVideoToCache(uri).absolutePath
        val audioOutputPath = getExternalFilesDir(null).toString() + "/share_with_me.m4a"

        FFmpegKit.executeAsync(
            "-y -i \"$videoPath\" -vn -map 0:a? -c:a copy \"$audioOutputPath\"",
            FFmpegSessionCompleteCallback { session ->
                onFirstAudioExtractResult(audioOutputPath, videoPath, session)
            }
        )
    }

    private fun onFirstAudioExtractResult(audioPath: String, videoPath: String, session: FFmpegSession) {
        if (ReturnCode.isSuccess(session.returnCode)) {
            runOnUiThread {
                toEngine(Uri.parse(audioPath), audioPath)
            }
        } else {
            FFmpegKit.executeAsync(
                "-y -i \"$videoPath\" -vn -map 0:a? -c:a aac -b:a 192k \"$audioPath\"",
                FFmpegSessionCompleteCallback { fallbackSession ->
                    onFallbackAudioExtractResult(audioPath, fallbackSession)
                }
            )
        }
    }

    private fun onFallbackAudioExtractResult(audioPath: String, session: FFmpegSession) {
        if (ReturnCode.isSuccess(session.returnCode)) {
            runOnUiThread {
                toEngine(Uri.parse(audioPath), audioPath)
            }
        } else {
            runOnUiThread {
                startActivity(Intent(this, WorkUserActivity::class.java))
                finish()
            }
        }
    }

    private fun toEngine(uri: Uri, path: String) {
        val intent = Intent(this, EngineActivity::class.java)
        intent.data = uri
        intent.putExtra("muri", path)
        startActivity(intent)
        finish()
    }

    private fun savePermanent(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(filesDir, "img_${System.currentTimeMillis()}.jpg")
            val fileOutputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)
            while (true) {
                val read = inputStream.read(buffer)
                if (read > 0) {
                    fileOutputStream.write(buffer, 0, read)
                } else {
                    inputStream.close()
                    fileOutputStream.close()
                    return file.absolutePath
                }
            }
            null // Unreachable but needed for compiler
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun handleImg(intent: Intent) {
        val uri = getParcelable(intent, Intent.EXTRA_STREAM, Uri::class.java)
        if (uri != null) {
            val imgIntent = Intent(this, EngineActivity::class.java)
            imgIntent.putExtra("img_bg", savePermanent(uri))
            startActivity(imgIntent)
            finish()
        }
    }

    private fun handleAudio(intent: Intent) {
        val uri = getParcelable(intent, Intent.EXTRA_STREAM, Uri::class.java)
        if (uri != null) {
            val audioIntent = Intent(this, EngineActivity::class.java)
            val processedFile = AudioUploadHelper.processAudioUriForUpload(this, uri, "share_with_me.mp3")
            audioIntent.data = uri
            audioIntent.putExtra("muri", processedFile?.absolutePath)
            startActivity(audioIntent)
            finish()
        }
    }

    @Suppress("DEPRECATION")
    private fun <T : Parcelable> getParcelable(intent: Intent, key: String, cls: Class<T>): T? {
        return if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(key, cls)
        } else {
            intent.getParcelableExtra(key)
        }
    }
}
