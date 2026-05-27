package hazem.nurmontage.videoquran

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import hazem.nurmontage.videoquran.databinding.ActivityAddReaderNameBinding

/**
 * Activity for adding or editing a reader's name.
 *
 * Presents an editable text field pre-populated with the existing name (if any).
 * Returns the result via [ActivityResult] with the following extras:
 * - `name` – the reader name entered
 * - `audio` – forwarded from the launching intent (MimeTypes.BASE_TYPE_AUDIO = "audio")
 * - `path_video_copy` – forwarded from the launching intent
 */
class AddReaderNameActivity : BaseActivity() {

    private lateinit var binding: ActivityAddReaderNameBinding

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            closeKeyboard()
            val intent = Intent().apply {
                putExtra(EXTRA_NAME, this@AddReaderNameActivity.intent.getStringExtra(EXTRA_NAME))
                putExtra(EXTRA_AUDIO, this@AddReaderNameActivity.intent.getStringExtra(EXTRA_AUDIO))
                putExtra(EXTRA_PATH_VIDEO_COPY, this@AddReaderNameActivity.intent.getStringExtra(EXTRA_PATH_VIDEO_COPY))
            }
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddReaderNameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // Apply window insets for edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, windowInsets ->
            val insets: Insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        hideSystemBars()

        // Cancel button – treat same as back
        binding.btnCancel.setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }

        // Done button – return the entered name
        binding.btnDone.setOnClickListener {
            closeKeyboard()
            val enteredName = binding.edtReader.text.toString().trim().replace("\n", " ")
            val intent = Intent().apply {
                putExtra(EXTRA_NAME, enteredName)
                putExtra(EXTRA_AUDIO, this@AddReaderNameActivity.intent.getStringExtra(EXTRA_AUDIO))
                putExtra(EXTRA_PATH_VIDEO_COPY, this@AddReaderNameActivity.intent.getStringExtra(EXTRA_PATH_VIDEO_COPY))
            }
            setResult(RESULT_OK, intent)
            finish()
        }

        // Pre-populate the EditText if an existing name was passed
        binding.edtReader.requestFocus()
        val existingName = intent.getStringExtra(EXTRA_NAME)
        if (existingName != null && existingName.length > 3) {
            binding.edtReader.setText(existingName)
        }

        showKeyboard()
    }

    override fun onPause() {
        closeKeyboard()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    /**
     * Show the soft keyboard for the EditText.
     */
    private fun showKeyboard() {
        try {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.edtReader, InputMethodManager.SHOW_IMPLICIT)
        } catch (_: Exception) {
        }
    }

    /**
     * Hide the soft keyboard.
     */
    private fun closeKeyboard() {
        try {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.edtReader.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val EXTRA_NAME = "name"
        // Original code used MimeTypes.BASE_TYPE_AUDIO which equals "audio"
        const val EXTRA_AUDIO = "audio"
        const val EXTRA_PATH_VIDEO_COPY = "path_video_copy"
    }
}
