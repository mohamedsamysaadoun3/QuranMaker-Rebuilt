package hazem.nurmontage.videoquran

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import hazem.nurmontage.videoquran.Utils.LocaleHelper
import hazem.nurmontage.videoquran.Utils.Utils
import hazem.nurmontage.videoquran.views.WaveformView

/**
 * Activity promoting the "Tuffah" companion app with before/after audio demos.
 * Plays two raw audio samples (before / after processing) with waveform visualization
 * and provides a link to install/open the Tuffah app.
 */
class AdsTuffahActivity : BaseActivity() {

    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnPlayPauseAfter: ImageButton
    private lateinit var currentBtn: ImageButton
    private lateinit var currentWave: WaveformView
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var waveformViewAfter: WaveformView
    private lateinit var waveformViewBefore: WaveformView

    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false
    private var currentResId: Int = R.raw.before

    private val updateProgressTask = object : Runnable {
        override fun run() {
            val mp = mediaPlayer ?: return
            if (!mp.isPlaying) return
            currentWave.setProgress(mp.currentPosition.toFloat() / mp.duration)
            handler.postDelayed(this, 50L)
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    // ── Lifecycle ──────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ads_tuufah)

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

        setString()

        // Back button
        findViewById<View>(R.id.btn_on_back).setOnClickListener { finish() }

        // Audio controls
        btnPlayPause = findViewById(R.id.btnPlayPause)
        waveformViewBefore = findViewById(R.id.waveformView)
        waveformViewAfter = findViewById(R.id.waveformView_after)
        btnPlayPauseAfter = findViewById(R.id.btnPlayPause_after)

        currentWave = waveformViewBefore
        currentBtn = btnPlayPause
        setupMediaPlayer(currentResId)

        btnPlayPause.setOnClickListener {
            switchAudio(R.raw.before, btnPlayPause, waveformViewBefore)
        }

        btnPlayPauseAfter.setOnClickListener {
            switchAudio(R.raw.after, btnPlayPauseAfter, waveformViewAfter)
        }

        waveformViewAfter.listener = object : WaveformView.OnWaveformClickListener {
            override fun onProgressChanged(progress: Float) {
                val mp = mediaPlayer ?: return
                mp.seekTo((mp.duration * progress).toInt())
                if (!mp.isPlaying) {
                    waveformViewAfter.setProgress(progress)
                }
            }
        }

        waveformViewBefore.listener = object : WaveformView.OnWaveformClickListener {
            override fun onProgressChanged(progress: Float) {
                val mp = mediaPlayer ?: return
                mp.seekTo((mp.duration * progress).toInt())
                if (!mp.isPlaying) {
                    waveformViewBefore.setProgress(progress)
                }
            }
        }

        // Tuffah install / open button
        findViewById<View>(R.id.btn_tuffah).setOnClickListener {
            if (Utils.isAppInstalled(this, TUFFAH_PACKAGE)) {
                packageManager.getLaunchIntentForPackage(TUFFAH_PACKAGE)?.let { launchIntent ->
                    startActivity(launchIntent)
                }
            } else {
                installTuffah()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (isPlaying) {
            togglePlayback()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateProgressTask)
        mediaPlayer?.let {
            it.release()
        }
        mediaPlayer = null
    }

    // ── UI setup ───────────────────────────────────────────────────────

    private fun setString() {
        findViewById<TextView>(R.id.tv_before).setText(R.string.before)
        findViewById<TextView>(R.id.tv_after).setText(R.string.after)

        val tvDownload = findViewById<TextView>(R.id.tv_download)
        if (!Utils.isAppInstalled(this, TUFFAH_PACKAGE)) {
            tvDownload.setText(R.string.download)
        } else {
            tvDownload.setText(R.string.openTuffah)
        }

        if (LocaleHelper.getLanguage(this) == "ar") {
            findViewById<View>(R.id.iv_en).visibility = View.GONE
            findViewById<View>(R.id.iv_ar).visibility = View.VISIBLE
        }
    }

    // ── Audio playback ─────────────────────────────────────────────────

    private fun setupMediaPlayer(resId: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, resId).apply {
            setOnCompletionListener { _ ->
                this@AdsTuffahActivity.isPlaying = false
                btnPlayPauseAfter.setImageResource(R.drawable.play_btn)
                btnPlayPause.setImageResource(R.drawable.play_btn)
                currentWave.setProgress(0.0f)
                handler.removeCallbacks(updateProgressTask)
            }
        }
    }

    private fun switchAudio(resId: Int, button: ImageButton, waveform: WaveformView) {
        btnPlayPauseAfter.setImageResource(R.drawable.play_btn)
        btnPlayPause.setImageResource(R.drawable.play_btn)
        currentWave = waveform
        currentBtn = button

        if (currentResId == resId) {
            togglePlayback()
            return
        }

        currentResId = resId
        handler.removeCallbacks(updateProgressTask)

        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
        }

        setupMediaPlayer(resId)
        isPlaying = false
        currentWave.setProgress(0.0f)
        currentBtn.setImageResource(R.drawable.play_btn)
        togglePlayback()
    }

    private fun togglePlayback() {
        val mp = mediaPlayer ?: return
        if (isPlaying) {
            mp.pause()
            currentBtn.setImageResource(R.drawable.play_btn)
            handler.removeCallbacks(updateProgressTask)
        } else {
            mp.start()
            currentBtn.setImageResource(R.drawable.pause_24px)
            handler.post(updateProgressTask)
        }
        isPlaying = !isPlaying
    }

    // ── Tuffah app install ─────────────────────────────────────────────

    private fun installTuffah() {
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$TUFFAH_PACKAGE")
            ).apply {
                setPackage("com.android.vending")
                addFlags(0x58000000.toInt()) // FLAG_ACTIVITY_NEW_TASK etc.
            }
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=$TUFFAH_PACKAGE")
                    )
                )
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(this, "Unable to open app store or browser.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val TUFFAH_PACKAGE = "hazem.tuffah.quranaudio"
    }
}
