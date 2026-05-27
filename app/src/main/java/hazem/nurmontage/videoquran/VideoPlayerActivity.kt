package hazem.nurmontage.videoquran

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class VideoPlayerActivity : BaseActivity() {

    private var btnPlay: ImageButton? = null
    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null
    private var videoUri: Uri? = null

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            releasePlayer()
            returnAct()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_video_player)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        videoUri = intent.data
        playerView = findViewById(R.id.playerView)
        setupButtons()
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        player?.play()
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        hideSystemBars()
    }

    private fun initializePlayer() {
        if (player != null || videoUri == null) return

        val exoPlayer = ExoPlayer.Builder(this)
            .setRenderersFactory(DefaultRenderersFactory(this).setEnableDecoderFallback(true))
            .setSeekBackIncrementMs(5000L)
            .setSeekForwardIncrementMs(5000L)
            .build()

        player = exoPlayer
        playerView?.setPlayer(exoPlayer)
        exoPlayer.setMediaItem(MediaItem.fromUri(videoUri!!))
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
        exoPlayer.prepare()
        exoPlayer.play()

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                retryWithFallbackDecoder()
            }
        })
    }

    private fun retryWithFallbackDecoder() {
        if (videoUri == null) return

        val newPlayer = ExoPlayer.Builder(this)
            .setRenderersFactory(DefaultRenderersFactory(this).setEnableDecoderFallback(true))
            .build()

        playerView?.setPlayer(newPlayer)
        newPlayer.setMediaItem(MediaItem.fromUri(videoUri!!))
        newPlayer.prepare()
        newPlayer.play()

        player?.release()
        player = newPlayer
    }

    private fun releasePlayer() {
        player?.let {
            playerView?.setUseController(false)
            playerView?.setPlayer(null)
            it.release()
            player = null
        }
    }

    private fun returnAct() {
        finish()
    }

    private fun setupButtons() {
        val btnBack = playerView?.findViewById<ImageButton>(R.id.btnBack)
        val btnRotate = playerView?.findViewById<ImageButton>(R.id.btnRotate)
        btnPlay = findViewById(R.id.btn_play_pause)

        btnBack?.setOnClickListener {
            releasePlayer()
            returnAct()
        }

        btnRotate?.setOnClickListener {
            if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }

        btnPlay?.setOnClickListener {
            player?.let { exoPlayer ->
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                    btnPlay?.setImageResource(R.drawable.play_arrow_24px)
                } else {
                    exoPlayer.play()
                    btnPlay?.setImageResource(R.drawable.pause_24px)
                }
            }
        }
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        setStatusBarColor(ViewCompat.MEASURED_STATE_MASK)
        setNavigationBarColor(ViewCompat.MEASURED_STATE_MASK)
    }

    override fun hideSystemBars() {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
