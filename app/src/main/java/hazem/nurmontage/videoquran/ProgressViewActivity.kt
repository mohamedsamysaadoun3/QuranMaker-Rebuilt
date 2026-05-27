package hazem.nurmontage.videoquran

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback
import com.arthenica.ffmpegkit.ReturnCode
import com.arthenica.ffmpegkit.Statistics
import com.arthenica.ffmpegkit.StatisticsCallback
import hazem.nurmontage.videoquran.Utils.AudioUtils
import hazem.nurmontage.videoquran.Utils.ColorUtils
import hazem.nurmontage.videoquran.Utils.Feadback
import hazem.nurmontage.videoquran.Utils.FfmpegCodecChecker
import hazem.nurmontage.videoquran.Utils.FileMediaScanner
import hazem.nurmontage.videoquran.Utils.LocalPersistence
import hazem.nurmontage.videoquran.Utils.LocaleHelper
import hazem.nurmontage.videoquran.common.Common
import hazem.nurmontage.videoquran.model.EntityBismilahTemplate
import hazem.nurmontage.videoquran.model.EntityMedia
import hazem.nurmontage.videoquran.model.EntityQuranTemplate
import hazem.nurmontage.videoquran.model.RenderManager
import hazem.nurmontage.videoquran.model.SquareBitmapModel
import hazem.nurmontage.videoquran.model.Template
import hazem.nurmontage.videoquran.views.ButtonCustumFont
import hazem.nurmontage.videoquran.views.SquareOutlineProgressBar
import hazem.nurmontage.videoquran.views.TextCustumFont
import org.apache.commons.io.FileUtils
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore

/**
 * Video export progress activity. Handles the entire FFmpeg-based video rendering
 * pipeline: pre-rendering layers (rounded masks, circles, video backgrounds, timers),
 * building the final FFmpeg command, running the export, and showing progress.
 */
class ProgressViewActivity : BaseActivity() {

    private var dialog: Dialog? = null
    private var isCancel = false
    @Volatile
    private var isDestroy = false
    private var mTemplate: Template? = null
    private var mUri: String? = null
    private var progressIndicator: SquareOutlineProgressBar? = null
    private var statistics: Statistics? = null
    private var workerThread: Thread? = null
    private val overlay = StringBuilder()
    private val renderManager = RenderManager()

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            showDialog()
        }
    }

    private val executor: Executor = Executors.newSingleThreadExecutor()
    private val idFfmpeg = mutableListOf<Long>()
    private val uiHandler = Handler(Looper.getMainLooper())

    private var displayedProgress = 0f
    private var targetProgress = 0f
    private val frameMs = 16
    private var isAnimating = false

    private val runnableProgress = Runnable {
        updateProgressDialog(statistics)
    }

    // region Lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(1536, 1536) // FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON before super
        super.onCreate(savedInstanceState)
        EdgeToEdge.enable(this)
        setContentView(R.layout.activity_progress_view)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        setStatusBarColor(ViewCompat.MEASURED_STATE_MASK)
        setNavigationBarColor(ViewCompat.MEASURED_STATE_MASK)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        wakeLockAquire()

        progressIndicator = findViewById(R.id.progress_horizontal)
        findViewById<View>(R.id.btn_cancel).setOnClickListener { showDialog() }

        try {
            startExport()
        } catch (_: Exception) {
            toStudio()
        }
    }

    override fun onPause() {
        super.onPause()
        cancelDialog()
    }

    override fun onDestroy() {
        isDestroy = true
        clearFFmpeg()
        stopAutoScroll()
        super.onDestroy()
    }

    // endregion

    // region Dialog

    fun showDialog() {
        val d = Dialog(this)
        dialog = d
        d.setCancelable(true)
        d.requestWindowFeature(1)
        d.window?.setLayout(-1, -2)
        d.window?.setBackgroundDrawable(ColorDrawable(0))
        val view = LayoutInflater.from(this).inflate(R.layout.layout_dialog, null as ViewGroup?)
        d.setContentView(view)

        val tvTitle = view.findViewById<TextCustumFont>(R.id.dialog_title)
        val tvMessage = view.findViewById<TextCustumFont>(R.id.dialog_message)
        val btnNo = view.findViewById<ButtonCustumFont>(R.id.dialog_no)
        val btnYes = view.findViewById<ButtonCustumFont>(R.id.dialog_yes)

        btnNo.setOnClickListener {
            isCancel = true
            toStudio()
        }
        btnYes.setOnClickListener {
            dialog?.dismiss()
        }

        if (LocaleHelper.getLanguage(this) == "ar") {
            tvMessage.text = "هل أنت متأكد من مغادرة هذا العمل؟"
            tvTitle.text = "خروج..."
            btnNo.text = "مغادرة"
            btnYes.text = "متابعة"
        } else {
            tvMessage.text = "Are you sure want to leave this work ?"
            tvTitle.text = "Exit..."
            btnNo.text = "Leave"
            btnYes.text = "Continue"
        }
        d.show()
    }

    private fun cancelDialog() {
        dialog?.dismiss()
        dialog = null
    }

    // endregion

    // region Export Flow

    private fun startExport() {
        val stringExtra = intent?.getStringExtra(Common.TEMPLATE) ?: return
        val template = LocalPersistence.readObjectFromFile(this, stringExtra) as? Template
        mTemplate = template
        if (template != null) {
            mUri = template.uri_video
        }
        prepareAllMedia(mTemplate?.entityMediaList) {
            FfmpegCodecChecker.detectCodecsAsync { codecInfo ->
                setupCommand(codecInfo)
            }
        }
    }

    private fun toStudio() {
        isCancel = true
        clearFFmpeg()
        // Navigate back to EngineActivity or finish
        finish()
    }

    private fun clearFFmpeg() {
        for (id in idFfmpeg) {
            try {
                FFmpegKit.cancel(id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // endregion

    // region AAC Check

    fun checkAacEncoder(context: Context) {
        try {
            val tempFile = File.createTempFile("aac_test", ".m4a", context.cacheDir)
            tempFile.deleteOnExit()
            FFmpegKit.executeAsync(
                "-y -f lavfi -i anullsrc=channel_layout=stereo:sample_rate=44100 -t 1 -c:a aac -b:a 64k ${tempFile.absolutePath}",
                { session ->
                    if (ReturnCode.isSuccess(session.returnCode)) {
                        Log.e("AAC workd", "AAC encoder is available!")
                    } else {
                        Log.e("AAC workd", "AAC encoder NOT supported in this build!")
                        Log.e("AAC workd", session.allLogsAsString)
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("AAC workd", "Error checking AAC: ${e.message}")
        }
    }

    // endregion

    // region FFmpeg Filter Helpers

    private fun mFadeFilter(st: Float, duration: Float, isIn: Boolean): String {
        var d = duration
        if (d - 0.05f <= 0f) d = 0.01f
        return "fade=t=${if (isIn) "in" else "out"}:st=${Math.abs(st)}:d=${Math.abs(d)}:alpha=1:color=white,fps=60,format=rgba"
    }

    private fun fadeInOut(fadeOutStart: Float, fadeInDuration: Float, fadeOutDuration: Float): String {
        var fd = fadeInDuration
        var fod = fadeOutDuration
        var fos = fadeOutStart
        if (fos <= 0f) fos = 0.01f
        if (fd - 0.05f <= 0f) fd = 0.01f
        if (fod - 0.05f <= 0f) fod = 0.01f
        return "fade=t=in:st=0:d=${Math.abs(fd)}:alpha=1:color=white,fps=${mTemplate?.fps ?: 30},format=rgba,fade=t=out:st=${Math.abs(fos)}:d=${Math.abs(fod)}:alpha=1:color=white,fps=${mTemplate?.fps ?: 30},format=rgba"
    }

    private fun fadeFilter(prefix: String, index: Int, st: Float, duration: Float, isIn: Boolean): String {
        val type = if (isIn) "in" else "out"
        return "${prefix}fade=t=$type:st=$st:d=${Math.abs(duration - 0.05f)}:alpha=1:color=white,fps=60,format=rgba[${type}_$index];"
    }

    private fun fadeFilter(label: String, st: Float, duration: Float, isIn: Boolean): String {
        val type = if (isIn) "in" else "out"
        return "[$label]fade=t=$type:st=$st:d=${Math.abs(duration - 0.05f)}:alpha=1:color=white,fps=60,format=rgba[${type}_$label];"
    }

    private fun fadeFilter(index: Int, st: Float, duration: Float, isIn: Boolean): String {
        val type = if (isIn) "in" else "out"
        return "[$index]fade=t=$type:st=$st:d=${Math.abs(duration - 0.05f)}:alpha=1:color=white,fps=60,format=rgba[${type}_$index];"
    }

    private fun slideX(delay: Float, duration: Float, base: Float, scale: Float, startVal: Float, endVal: Float): String {
        val t = "clip((t-$delay)/$duration,0,1)"
        return "'$base+((${startVal}+(${endVal - startVal})*(${t}*${t}*(3-2*${t}))))*$scale'"
    }

    private fun mSlideX(delay: Float, duration: Float, base: Float, scale: Float, startVal: Float, endVal: Float): String {
        val t = "clip((t-$delay)/$duration,0,1)"
        return "$base+((${startVal}+(${endVal - startVal})*(${t}*${t}*(3-2*${t}))))*$scale"
    }

    // endregion

    // region Mask Generation

    private fun getOrCreateMask(width: Int, height: Int, radius: Int): File {
        val file = File(filesDir, "mask_${width}x${height}_r$radius.png")
        if (file.exists()) return file
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(0, PorterDuff.Mode.CLEAR)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.WHITE
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rect, radius.toFloat(), radius.toFloat(), paint)
        try {
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
        } catch (_: Exception) {
        }
        return file
    }

    private fun createTransparentBg(width: Int, height: Int): File {
        val file = File(filesDir, "bg_tr_.png")
        if (file.exists()) return file
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        try {
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
        } catch (_: Exception) {
        }
        return file
    }

    private fun getOrCreateMaskCircle(width: Int, height: Int): File {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.WHITE
        canvas.drawCircle(width / 2f, height / 2f, Math.min(width, height) / 2f, paint)
        val file = File(mTemplate?.folder_template ?: filesDir.absolutePath, "circle_${width}x${height}.png")
        try {
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
        } catch (_: Exception) {
        }
        return file
    }

    // endregion

    // region Timer Generation

    private fun generateVideoTimer(
        durationMs: Int,
        countDownLatch: CountDownLatch,
        semaphore: Semaphore
    ): String? {
        val outputPath = (mTemplate?.folder_template ?: "") + "/timer.mov"
        val maxSeconds = Math.max(durationMs / 1000, 1)
        renderManager.addTask("timer prerender", maxSeconds)

        val timeModel = mTemplate?.mTimeModel ?: return null
        val posXRight = timeModel.posXRight
        val color = timeModel.color
        val size = timeModel.size
        val fontPath = filesDir.absolutePath + "/NotoNaskhArabic.ttf"

        val args = arrayListOf(
            "-y", "-f", "lavfi", "-i",
            "color=size=${Math.round(timeModel.width_bitmap_progress * 1.3f)}x${timeModel.height_bitmap_progress}:rate=10:duration=$maxSeconds:color=${if (ColorUtils.isColorDark(Color.parseColor(color))) "black@0" else "white@0"},format=rgba"
        )
        val totalSec = maxSeconds + 1
        args.add("-vf")
        args.add("drawtext=fontfile='$fontPath':text='%{eif\\:trunc(t/60)\\:d\\:2}\\:%{eif\\:trunc(mod(t\\,60))\\:d\\:2}':x=0.0:y=0.0:fontsize=$size:fontcolor=$color,drawtext=fontfile='$fontPath':text='-%{eif\\:trunc(($totalSec-t)/60)\\:d\\:2}\\:%{eif\\:trunc(mod($totalSec-t\\,60))\\:d\\:2}':x=$posXRight:y=0.0:fontsize=$size:fontcolor=$color")
        args.addAll(listOf("-c:v", "qtrle", "-pix_fmt", "argb", "-preset", "veryfast", "-avoid_negative_ts", "make_zero", outputPath))

        try {
            semaphore.acquire()
            val sessionId = FFmpegKit.executeWithArgumentsAsync(
                args.toTypedArray(),
                { _ -> updateNext(countDownLatch, semaphore) },
                null,
                StatisticsCallback { _ -> }
            ).sessionId
            idFfmpeg.add(sessionId)
            return outputPath
        } catch (_: InterruptedException) {
            renderManager.nextTask()
            countDownLatch.countDown()
            return null
        }
    }

    // endregion

    // region Pre-Render Pipeline

    private fun runPreRender(
        inputPath: String,
        maskPath: String?,
        filterComplex: String,
        durationMs: Int,
        outputPath: String,
        isAlpha: Boolean,
        countDownLatch: CountDownLatch,
        semaphore: Semaphore,
        codec: String?
    ): String? {
        val args = arrayListOf("-hide_banner", "-y", "-stream_loop", "-1", "-i", inputPath)
        if (maskPath != null) {
            args.addAll(listOf("-i", maskPath))
        }
        args.addAll(listOf("-filter_complex", filterComplex))

        if (isAlpha) {
            args.addAll(listOf("-c:v", "qtrle", "-pix_fmt", "rgba"))
        } else if (codec != null) {
            args.addAll(listOf("-threads", "0", "-c:v", codec, "-preset", "fast", "-crf", "18"))
        } else {
            args.addAll(listOf("-b:v", "4M"))
        }

        args.addAll(listOf("-r", "${mTemplate?.fps ?: 30}", "-t", "${Math.max(durationMs, 500)}ms"))
        if (!isAlpha) {
            args.addAll(listOf("-movflags", "+faststart"))
        }
        args.add(outputPath)

        try {
            semaphore.acquire()
            val sessionId = FFmpegKit.executeWithArgumentsAsync(
                args.toTypedArray(),
                { _ -> updateNext(countDownLatch, semaphore) },
                null,
                StatisticsCallback { _ -> }
            ).sessionId
            idFfmpeg.add(sessionId)
            return outputPath
        } catch (_: InterruptedException) {
            renderManager.nextTask()
            countDownLatch.countDown()
            return null
        }
    }

    private fun updateNext(countDownLatch: CountDownLatch?, semaphore: Semaphore?) {
        renderManager.nextTask()
        semaphore?.release()
        countDownLatch?.countDown()
    }

    fun preRenderMask_Rounded(
        model: SquareBitmapModel,
        durationMs: Int,
        countDownLatch: CountDownLatch,
        semaphore: Semaphore
    ): String? {
        val inputVideo = mTemplate?.uri_media_video ?: return null
        val outputPath = (mTemplate?.folder_template ?: "") + "/rounded_${System.currentTimeMillis()}.mov"
        val maxDim = Math.max(mTemplate?.width ?: 0, mTemplate?.height ?: 0)

        var right = Math.round(model.right)
        var bottom = Math.round(model.bottom)
        var leftSquare = Math.round(model.lef_square)
        var topSquare = Math.round(model.top_square)
        var widthSquare = Math.round(model.width_sqaure)
        var heightSquare = Math.round(model.height_square)
        if (widthSquare and 1 == 1) widthSquare++
        if (heightSquare and 1 == 1) heightSquare++

        val maskPath = getOrCreateMask(widthSquare, heightSquare, model.raduis.toInt()).absolutePath
        val filter = "[0:v]scale=$maxDim:$maxDim:force_original_aspect_ratio=increase,crop=$right:$bottom:$leftSquare:$topSquare,scale=$widthSquare:$heightSquare:flags=lanczos[v];[v][1:v]alphamerge,format=rgba"

        return runPreRender(inputVideo, maskPath, filter, durationMs, outputPath, true, countDownLatch, semaphore, null)
    }

    fun preRenderMask_Circle(
        model: SquareBitmapModel,
        durationMs: Int,
        countDownLatch: CountDownLatch,
        semaphore: Semaphore
    ): String? {
        val inputVideo = mTemplate?.uri_media_video ?: return null
        val outputPath = (mTemplate?.folder_template ?: "") + "/circle_${System.currentTimeMillis()}.mov"
        val maxDim = Math.max(mTemplate?.width ?: 0, mTemplate?.height ?: 0)

        var right = Math.round(model.right)
        var bottom = Math.round(model.bottom)
        var leftSquare = Math.round(model.lef_square)
        var topSquare = Math.round(model.top_square)
        var widthSquare = Math.round(model.width_sqaure)
        var heightSquare = Math.round(model.height_square)
        if (widthSquare and 1 == 1) widthSquare++
        if (heightSquare and 1 == 1) heightSquare++

        val maskPath = getOrCreateMaskCircle(widthSquare, heightSquare).absolutePath
        val filter = "[0:v]scale=$maxDim:$maxDim:force_original_aspect_ratio=increase,crop=$right:$bottom:$leftSquare:$topSquare,scale=$widthSquare:$heightSquare:flags=lanczos[v];[v][1:v]alphamerge,format=rgba"

        return runPreRender(inputVideo, maskPath, filter, durationMs, outputPath, true, countDownLatch, semaphore, null)
    }

    fun preRender_NoMask(
        model: SquareBitmapModel,
        durationMs: Int,
        countDownLatch: CountDownLatch,
        semaphore: Semaphore,
        codec: String?
    ): String? {
        val inputVideo = mTemplate?.uri_media_video ?: return null
        val outputPath = (mTemplate?.folder_template ?: "") + "/nomask_${System.currentTimeMillis()}.mp4"
        val maxDim = Math.max(mTemplate?.width ?: 0, mTemplate?.height ?: 0)

        var right = Math.round(model.right)
        var bottom = Math.round(model.bottom)
        var leftSquare = Math.round(model.lef_square)
        var topSquare = Math.round(model.top_square)
        var widthSquare = Math.round(model.width_sqaure)
        var heightSquare = Math.round(model.height_square)
        if (widthSquare and 1 == 1) widthSquare++
        if (heightSquare and 1 == 1) heightSquare++

        val filter = "scale=$maxDim:$maxDim:force_original_aspect_ratio=increase,crop=$right:$bottom:$leftSquare:$topSquare,scale=$widthSquare:$heightSquare:flags=lanczos,format=yuv420p"

        return runPreRender(inputVideo, null, filter, durationMs, outputPath, false, countDownLatch, semaphore, codec)
    }

    fun preRenderVideo(
        durationMs: Int,
        countDownLatch: CountDownLatch,
        semaphore: Semaphore,
        codec: String?
    ): String? {
        val inputVideo = mTemplate?.uri_media_video ?: return null
        val outputPath = (mTemplate?.folder_template ?: "") + "/layer_video_${System.currentTimeMillis()}.mp4"
        val maxDim = Math.max(mTemplate?.width ?: 0, mTemplate?.height ?: 0)
        val width = mTemplate?.width ?: 0
        val height = mTemplate?.height ?: 0

        val filter = "[0:v]scale=$maxDim:$maxDim:force_original_aspect_ratio=increase:flags=lanczos,crop=$width:$height:${(width - width) / 2}:${(height - height) / 2}[v];[v][1:v]overlay,format=rgba"

        val args = arrayListOf("-hide_banner", "-y", "-stream_loop", "-1", "-i", inputVideo)
        val bgFile = File(mTemplate?.uri_bg_ffmpeg ?: "")
        if (bgFile.exists() && bgFile.isFile) {
            mTemplate?.uri_bg_ffmpeg?.let { args.addAll(listOf("-i", it)) }
            args.addAll(listOf("-filter_complex", filter))
            if (codec != null) {
                args.addAll(listOf("-threads", "0", "-c:v", codec, "-preset", "fast", "-crf", "18"))
            } else {
                args.addAll(listOf("-b:v", "4M"))
            }
            args.addAll(listOf("-r", "${mTemplate?.fps ?: 30}", "-t", "${Math.max(durationMs, 500)}ms", "-movflags", "+faststart", "-an", outputPath))
            try {
                semaphore.acquire()
                val sessionId = FFmpegKit.executeWithArgumentsAsync(
                    args.toTypedArray(),
                    { _ -> updateNext(countDownLatch, semaphore) },
                    null,
                    StatisticsCallback { _ -> }
                ).sessionId
                idFfmpeg.add(sessionId)
                return outputPath
            } catch (_: InterruptedException) {
                renderManager.nextTask()
                countDownLatch.countDown()
                return null
            }
        }
        updateNext(countDownLatch, semaphore)
        return null
    }

    fun preRenderVideoHue(
        durationMs: Int,
        countDownLatch: CountDownLatch,
        semaphore: Semaphore,
        codec: String?
    ): String? {
        val inputVideo = mTemplate?.uri_media_video ?: return null
        val outputPath = (mTemplate?.folder_template ?: "") + "/layer_video_${System.currentTimeMillis()}.mp4"
        val maxDim = Math.max(mTemplate?.width ?: 0, mTemplate?.height ?: 0)
        val width = mTemplate?.width ?: 0
        val height = mTemplate?.height ?: 0
        val tm = mTemplate?.mTimeModel
        val ept = mTemplate?.entityProgressTemplate

        val filter = "[0:v]scale=$maxDim:$maxDim:force_original_aspect_ratio=increase:flags=lanczos,hue=s=0,crop=$width:$height:${(width - width) / 2}:${(height - height) / 2}[main];[main][1]overlay[fm];[2:v]loop=loop=-1:size=1:start=0,setpts=N/FRAME_RATE/TB[lineProg];[3:v]loop=loop=-1:size=1:start=0,setpts=N/FRAME_RATE/TB[lineBg];[lineProg][lineBg]overlay=x=${-(tm?.width_bitmap_progress ?: 0)} + ((cos((t / (${durationMs / 1000.0}) + 1) * PI) / 2 + 0.5) * ${(tm?.width_bitmap_progress ?: 0) - (tm?.progress_offset ?: 0)}):y=0[bgApplied];[fm][bgApplied]overlay=${ept?.left ?: 0}:${ept?.top ?: 0}"

        val args = arrayListOf("-hide_banner", "-y", "-i", inputVideo)
        val bgFile = File(mTemplate?.uri_bg_ffmpeg ?: "")
        if (bgFile.exists() && bgFile.isFile) {
            mTemplate?.uri_bg_ffmpeg?.let { args.addAll(listOf("-i", it)) }
            args.addAll(listOf("-i", (mTemplate?.folder_template ?: "") + "/line_progress.png"))
            args.addAll(listOf("-i", (mTemplate?.folder_template ?: "") + "/line_bg.png"))
            args.addAll(listOf("-filter_complex", filter))
            if (codec != null) {
                args.addAll(listOf("-c:v", codec, "-preset", "fast", "-crf", "18"))
            } else {
                args.addAll(listOf("-c:v", "libx264", "-preset", "veryfast", "-crf", "18"))
            }
            args.addAll(listOf("-r", "${mTemplate?.fps ?: 30}", "-t", "${Math.max(durationMs, 500)}ms", "-movflags", "+faststart", "-an", outputPath))
            try {
                semaphore.acquire()
                val sessionId = FFmpegKit.executeWithArgumentsAsync(
                    args.toTypedArray(),
                    { _ -> updateNext(countDownLatch, semaphore) },
                    null,
                    StatisticsCallback { _ -> }
                ).sessionId
                idFfmpeg.add(sessionId)
                return outputPath
            } catch (_: InterruptedException) {
                renderManager.nextTask()
                countDownLatch.countDown()
                return null
            }
        }
        updateNext(countDownLatch, semaphore)
        return null
    }

    // endregion

    // region Basmala

    /**
     * Add bismilah overlay to the FFmpeg command.
     *
     * NOTE: The original Java method had 3057 instructions that JADX could not decompile.
     * This is a stub that should be filled in during Phase 7 (Engine/Render) when the
     * full render pipeline is implemented.
     */
    private fun addBasmala(
        entity: EntityBismilahTemplate,
        inputIndex: Int,
        semaphore: Semaphore,
        countDownLatch: CountDownLatch,
        commandList: List<String>,
        totalDurationSec: Float
    ): Int {
        // TODO: Phase 7 — Full bismilah overlay FFmpeg command construction
        // The original method was ~3057 instructions (JADX couldn't decompile).
        // It builds FFmpeg filter chains for the bismilah text/animation overlay.
        return inputIndex
    }

    // endregion

    // region Media Preparation

    fun prepareAllMedia(mediaList: List<EntityMedia>?, callback: Runnable?) {
        Executors.newSingleThreadExecutor().execute {
            try {
                if (mediaList != null) {
                    if (mediaList.isNotEmpty()) {
                        for (entity in mediaList) {
                            if (entity != null) {
                                try {
                                    val entityUri = entity.uri
                                        if (entity.end >= entity.start && entity.path_ffmpeg_effect == null && entityUri != null) {
                                        val filePath = if (entityUri.startsWith("http")) {
                                            AudioUtils.downloadFile(this, entityUri, mTemplate?.folder_template ?: "")
                                        } else {
                                            AudioUtils.copyFromUri(this, Uri.parse(entityUri), mTemplate?.folder_template ?: "")
                                        }
                                        if (filePath != null) {
                                            if (filePath != null) entity.path_ffmpeg = filePath ?: ""
                                            if (filePath != null) entity.path_ffmpeg_effect = filePath ?: ""
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        callback?.run()
                    } else {
                        Log.w("AudioUtils", "Media list is null or empty")
                        callback?.run()
                    }
                } else {
                    Log.w("AudioUtils", "Media list is null or empty")
                    callback?.run()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback?.run()
            }
        }
    }

    // endregion

    // region Command Setup

    /**
     * Build the full FFmpeg export command.
     *
     * NOTE: The original Java method had 8078 instructions that JADX could not decompile.
     * This is a structural stub that sets up the basic pipeline and calls the export.
     * The full implementation will be completed in Phase 7 (Engine/Render).
     */
    fun setupCommand(codecInfo: FfmpegCodecChecker.CodecInfo) {
        // TODO: Phase 7 — Full FFmpeg command construction
        // The original setupCommand had ~8078 instructions (JADX couldn't decompile).
        // It constructs the complete FFmpeg filter graph based on template entities:
        // - Quran text overlays with timing
        // - Bismilah overlays
        // - Background video/picture layers
        // - Progress bar overlay
        // - Timer overlay
        // - Audio mixing
        // - Final encoding with codec selection
        //
        // The basic structure is:
        // 1. Pre-render all layers (masks, circles, videos, timer) using the methods above
        // 2. Build a filter_complex string combining all layers
        // 3. Run the final FFmpeg command with progress tracking

        val template = mTemplate ?: return
        val codec = if (codecInfo.isHardwareSupported) codecInfo.preferredCodec else null

        // For now, call the export with a minimal command structure
        // The full implementation will be added in Phase 7
        try {
            Feadback.sendRenderStart(this)
        } catch (_: Exception) {
        }

        // TODO: Build full command list and call export(command)
    }

    // endregion

    // region Export

    private fun export(command: Array<String>) {
        if (isDestroy || isCancel) return
        try {
            Feadback.sendRenderStart(this)
        } catch (_: Exception) {
        }

        workerThread = Thread {
            try {
                val sessionId = FFmpegKit.executeWithArgumentsAsync(
                    command,
                    { session ->
                        uiHandler.post {
                            if (ReturnCode.isSuccess(session.returnCode)) {
                                onExportSuccess()
                            } else if (ReturnCode.isCancel(session.returnCode)) {
                                Log.w("ProgressView", "Export cancelled")
                            } else {
                                Log.e("ProgressView", "Export failed: ${session.allLogsAsString}")
                                onExportFailed()
                            }
                        }
                    },
                    null,
                    StatisticsCallback { newStatistics: com.arthenica.ffmpegkit.Statistics ->
                        statistics = newStatistics
                        uiHandler.post { updateProgressDialog(newStatistics) }
                    }
                ).sessionId
                idFfmpeg.add(sessionId)
            } catch (e: Exception) {
                e.printStackTrace()
                uiHandler.post { onExportFailed() }
            }
        }
        workerThread?.start()
    }

    private fun onExportSuccess() {
        if (isFinishing) return
        try {
            Feadback.sendRenderEnd(this)
        } catch (_: Exception) {
        }

        val uri = mUri ?: return
        FileMediaScanner.scanFile(this, uri)

        // Navigate to video player or done screen
        val intent = Intent(this, VideoViewActivity::class.java)
        intent.putExtra(Common.TEMPLATE, mTemplate?.idTemplate)
        startActivity(intent)
        finish()
    }

    private fun onExportFailed() {
        if (isFinishing) return
        try {
            Feadback.sendRenderEnd(this)
        } catch (_: Exception) {
        }
        toStudio()
    }

    // endregion

    // region Concat

    private fun concatVideoSegments(segmentPaths: List<String>): String? {
        return try {
            val fileList = File((mTemplate?.folder_template ?: "") + "/file_list.txt")
            val writer = BufferedWriter(FileWriter(fileList))
            for (path in segmentPaths) {
                writer.write("file '$path'\n")
            }
            writer.close()

            val outputPath = (mTemplate?.folder_template ?: "") + "/concat_${System.currentTimeMillis()}.mp4"
            val args = arrayOf(
                "-y", "-f", "concat", "-safe", "0", "-i", fileList.absolutePath,
                "-c", "copy", outputPath
            )

            val session = FFmpegKit.executeWithArguments(args)
            if (ReturnCode.isSuccess(session.returnCode)) outputPath else null
        } catch (e: Exception) {
            Log.e("ProgressView", "Concat failed: ${e.message}")
            null
        }
    }

    // endregion

    // region Progress

    private fun updateProgressDialog(statistics: Statistics?) {
        if (isFinishing || isDestroy) return
        val pi = progressIndicator ?: return
        val template = mTemplate ?: return

        try {
            val timeMs = statistics?.time?.toLong() ?: 0L
            val durationMs = template.duration.toLong()
            var progressValue = 0f
            if (durationMs > 0) {
                progressValue = (timeMs * 100f / durationMs).coerceIn(0f, 100f)
                pi.progress = (timeMs * 100f / durationMs).toInt().coerceIn(0, 100)
            }

            // Update render manager progress for pre-render phases
            val rmProgress = renderManager.getProgress()
            if (rmProgress > 0) {
                val combinedProgress = (rmProgress * 100f / renderManager.totalWeight).coerceIn(0f, 100f)
                // Use the higher of FFmpeg progress or render manager progress
                val finalProgress = maxOf(progressValue, combinedProgress).toInt()
                pi.progress = finalProgress
            }
        } catch (_: Exception) {
        }
    }

    private fun stopAutoScroll() {
        uiHandler.removeCallbacks(runnableProgress)
    }

    // endregion
}
