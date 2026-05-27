package hazem.nurmontage.videoquran

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.RelativeLayout
import android.widget.VideoView
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class PlayVideoActivity : BaseActivity() {

    private var mediaController: MediaController? = null
    private var parentLayout: RelativeLayout? = null
    private var videoView: VideoView? = null

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            pause()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_video)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        WindowCompat.setDecorFitsSystemWindows(window, true)
        setStatusBarColor(ViewCompat.MEASURED_STATE_MASK)
        setNavigationBarColor(ViewCompat.MEASURED_STATE_MASK)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, windowInsetsCompat ->
            val insets: Insets = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsetsCompat
        }

        parentLayout = findViewById(R.id.parentLayout)

        if (intent != null && intent.data != null) {
            val data: Uri = intent.data ?: return
            videoView = findViewById(R.id.videoView)

            mediaController = MediaController(this).apply {
                setMediaPlayer(videoView)
                setAnchorView(videoView)
            }

            videoView?.apply {
                setMediaController(mediaController)
                setVideoURI(data)
                setOnCompletionListener {
                    if (mediaController == null || mediaController?.isShowing == true) return@setOnCompletionListener
                    mediaController?.show()
                }
                setOnPreparedListener { mediaPlayer ->
                    adjustVideoViewSize(mediaPlayer)
                }
                start()
            }
        }

        findViewById<android.view.View>(R.id.btn_on_back).setOnClickListener {
            pause()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        pause()
        super.onPause()
    }

    override fun onDestroy() {
        videoView?.let {
            it.pause()
        }
        videoView = null
        super.onDestroy()
    }

    private fun adjustVideoViewSize(mediaPlayer: MediaPlayer?) {
        if (mediaPlayer == null) return

        val videoWidth = mediaPlayer.videoWidth
        val videoHeight = mediaPlayer.videoHeight
        val parent = parentLayout ?: return

        var width = parent.width
        var height = parent.height

        val videoAspectRatio = videoWidth.toFloat() / videoHeight.toFloat()
        val screenAspectRatio = width.toFloat() / height.toFloat()

        if (videoAspectRatio > screenAspectRatio) {
            height = (width.toFloat() / videoAspectRatio).toInt()
        } else {
            width = (height.toFloat() * videoAspectRatio).toInt()
        }

        val layoutParams = RelativeLayout.LayoutParams(width, height).apply {
            addRule(RelativeLayout.CENTER_IN_PARENT)
        }
        videoView?.layoutParams = layoutParams
    }

    private fun pause() {
        videoView?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }
}
