package hazem.nurmontage.videoquran

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import hazem.nurmontage.videoquran.Utils.BillingPreferences
import hazem.nurmontage.videoquran.Utils.MyPrefereces
import hazem.nurmontage.videoquran.common.Common
import hazem.nurmontage.videoquran.views.ButtonCustumFont
import hazem.nurmontage.videoquran.views.CropView
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * Activity for cropping a bitmap with a movable/scaleable crop rectangle.
 * The bitmap and initial crop rect come from [Common.bitmap] / [Common.rect].
 * Returns the cropped result plus x/y/w/h position extras.
 */
class CropBitmapActivity : BaseActivity() {

    companion object {
        var isActive: Boolean = false
    }

    private var cropView: CropView? = null
    private var dialog: Dialog? = null

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            cancel()
        }
    }

    // ── Lifecycle ──────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop_bitmap)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        setStatusBarColor(ViewCompat.MEASURED_STATE_MASK)
        setNavigationBarColor(ViewCompat.MEASURED_STATE_MASK)

        isActive = true

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        val resources = resources
        if (resources != null) {
            findViewById<TextCustumFont>(R.id.tv_tittle_fragment)
                .setText(R.string.choice_screen_ipod)
        }

        init()
    }

    override fun onPause() {
        super.onPause()
        cancelDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        isActive = false
    }

    // ── Private helpers ────────────────────────────────────────────────

    private fun cancel() {
        setResult(RESULT_CANCELED)
        finish()
    }

    private fun cancelDialog() {
        dialog?.let {
            if (it.isShowing) it.dismiss()
        }
        dialog = null
    }

    private fun init() {
        findViewById<View>(R.id.btn_cancel).setOnClickListener { cancel() }

        if (Common.bitmap == null || Common.rect == null) return

        cropView = findViewById<CropView>(R.id.crop_view).also { cv ->
            cv.post {
                Common.bitmap?.let { bmp ->
                    cv.setBitmap(bmp, Common.rect!!, Common.radius, MyPrefereces.isShowHint(this@CropBitmapActivity))
                }
            }
        }

        findViewById<ButtonCustumFont>(R.id.btn_done).apply {
            setText(R.string.done)
            setOnClickListener {
                if (!BillingPreferences.isSubscribed(this@CropBitmapActivity)) {
                    dialogPremium()
                    return@setOnClickListener
                }
                if (!MyPrefereces.isShowHint(this@CropBitmapActivity)) {
                    MyPrefereces.putShowHint(this@CropBitmapActivity)
                }

                val cv = cropView ?: return@setOnClickListener
                Common.bitmap = cv.croppedBitmap()
                Common.rect = cv.rectSquare()

                Intent().apply {
                    putExtra("x", cv.getmX())
                    putExtra("y", cv.getmY())
                    putExtra("w", cv.getmW())
                    putExtra("h", cv.getmH())
                    setResult(RESULT_OK, this)
                }
                finish()
            }
        }
    }

    // ── Premium dialog ─────────────────────────────────────────────────

    fun dialogPremium() {
        try {
            cancelDialog()
            Dialog(this).apply {
                dialog = this
                setCancelable(true)
                requestWindowFeature(1)
                window?.setLayout(-1, -2)
                window?.setBackgroundDrawable(ColorDrawable(0))

                val view = LayoutInflater.from(this@CropBitmapActivity)
                    .inflate(R.layout.layout_dialog, null as ViewGroup?)
                setContentView(view)

                view.findViewById<View>(R.id.dialog_title).visibility = View.GONE
                view.findViewById<View>(R.id.img_pro).visibility = View.VISIBLE

                view.findViewById<TextCustumFont>(R.id.dialog_message).apply {
                    setText(R.string.unlock_premium)
                    gravity = Gravity.CENTER
                }

                view.findViewById<ButtonCustumFont>(R.id.dialog_no).apply {
                    setText(R.string.no)
                    setOnClickListener { cancelDialog() }
                }

                view.findViewById<ButtonCustumFont>(R.id.dialog_yes).apply {
                    setText(R.string.yes)
                    setOnClickListener {
                        toProVersion()
                        cancelDialog()
                    }
                }

                show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toProVersion() {
        Intent(this, ProVersionActivity::class.java).apply {
            addFlags(0x10000) // FLAG_ACTIVITY_CLEAR_TOP
            startActivity(this)
        }
        overridePendingTransition(0, 0)
    }
}
