package hazem.nurmontage.videoquran

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import hazem.nurmontage.videoquran.common.Common
import hazem.nurmontage.videoquran.databinding.ActivityChoiceBgFromVideoBinding
import hazem.nurmontage.videoquran.views.VideoFrameSelectorView

/**
 * Activity that allows the user to choose a background frame from a video.
 *
 * The user scrubs through the video frames using a [VideoFrameSelectorView],
 * previews the selected frame in an ImageView, and confirms the selection.
 * The selected bitmap is stored in [Common.bitmap] and the result is
 * returned as [RESULT_OK].
 */
class ChoiceBgFromVideoActivity : BaseActivity() {

    private lateinit var binding: ActivityChoiceBgFromVideoBinding

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            cancel()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChoiceBgFromVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // Dark status/navigation bar for video preview
        setStatusBarColor(ViewCompat.MEASURED_STATE_MASK)
        setNavigationBarColor(ViewCompat.MEASURED_STATE_MASK)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        // Apply window insets for edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, windowInsets ->
            val insets: Insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        // Set title
        binding.tvTittleFragment.text = getString(R.string.choice_bg)

        // Cancel button
        binding.btnCancel.setOnClickListener {
            cancel()
        }

        // Initialize video frame selector if a URI was provided
        if (intent != null) {
            init(intent.data)
        }
    }

    /**
     * Cancel selection and return RESULT_CANCELED.
     */
    private fun cancel() {
        setResult(RESULT_CANCELED)
        finish()
    }

    /**
     * Set up the video frame selector with the given [uri].
     */
    private fun init(uri: Uri?) {
        if (uri == null) return

        val frameSelectorView = binding.frameSelectorView
        frameSelectorView.setVideoUri(uri)

        frameSelectorView.onFrameSelectedListener = object : VideoFrameSelectorView.OnFrameSelectedListener {
            override fun onFrameSelected(index: Int, bitmap: Bitmap) {
                binding.ivView.setImageBitmap(bitmap)
            }
        }

        // Done button – store the selected frame bitmap in Common and return
        binding.btnDone.setOnClickListener {
            val bitmapFrame = frameSelectorView.getFrameBitmap()
            if (bitmapFrame != null) {
                Common.bitmap = bitmapFrame.bitmap
            }
            setResult(RESULT_OK, Intent())
            finish()
        }
    }
}
