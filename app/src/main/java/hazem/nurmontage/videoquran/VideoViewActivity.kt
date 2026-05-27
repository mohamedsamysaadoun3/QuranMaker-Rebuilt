package hazem.nurmontage.videoquran

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import hazem.nurmontage.videoquran.Utils.AppUtils
import hazem.nurmontage.videoquran.Utils.LocalPersistence
import hazem.nurmontage.videoquran.Utils.Utils
import hazem.nurmontage.videoquran.common.Common
import hazem.nurmontage.videoquran.fragment.RatingBottomSheetDialog
import hazem.nurmontage.videoquran.views.ButtonCustumFont
import hazem.nurmontage.videoquran.views.TextCustumFont
import hazem.nurmontage.videoquran.views.TextCustumFontBold
import java.io.File

/**
 * Activity showing a video thumbnail with options to play, share, go home,
 * return to studio, or get help via WhatsApp.
 * Also handles the in-app rating dialog.
 */
class VideoViewActivity : BaseActivity() {

    private var btnPlayPause: ImageView? = null
    private var dialog: Dialog? = null
    private var idTemplate: String? = null
    private var mUri: String? = null
    private lateinit var parentLayout: RelativeLayout
    private var reader: String? = null
    private var surah: String? = null
    private var videoView: ImageView? = null

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            toStudio()
        }
    }

    // ── Lifecycle ──────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_view)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        setSystemUiAppearance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        LocalPersistence.deleteTemplate(this, Common.TEMPLATE_TMP)

        if (intent != null) {
            val data = intent.data
            idTemplate = intent.getStringExtra(Common.TEMPLATE)
            reader = intent.getStringExtra(Common.READER)
            surah = intent.getStringExtra(Common.SURAH)
            parentLayout = findViewById(R.id.parentLayout)

            if (data != null) {
                mUri = data.toString()
                btnPlayPause = findViewById(R.id.btn_play_pause)

                videoView = findViewById<ImageView>(R.id.videoView).also { iv ->
                    iv.post {
                        Glide.with(this@VideoViewActivity as FragmentActivity)
                            .asBitmap()
                            .load(mUri)
                            .frame(1_000_000L)
                            .centerInside()
                            .override(
                                maxOf(50, (parentLayout.width * 0.9f).toInt()),
                                maxOf(50, (parentLayout.height * 0.9f).toInt())
                            )
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .signature(ObjectKey(AppUtils.getVersionName(this@VideoViewActivity)))
                            .into(iv)
                    }
                }

                videoView?.setOnClickListener {
                    Intent(this, VideoPlayerActivity::class.java).apply {
                        setData(data)
                        addFlags(0x10000) // FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(this)
                    }
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                }
            }
        }

        // Tuffah install / open
        findViewById<View>(R.id.btn_tuffah).setOnClickListener {
            if (Utils.isAppInstalled(this, TUFFAH_PACKAGE)) {
                packageManager.getLaunchIntentForPackage(TUFFAH_PACKAGE)?.let { launchIntent ->
                    startActivity(launchIntent)
                }
            } else {
                installTuffah()
            }
        }

        // Home button
        findViewById<View>(R.id.btn_home).setOnClickListener {
            startActivity(Intent(this, WorkUserActivity::class.java))
            finish()
        }

        // Share label
        val tvShare = findViewById<TextCustumFont>(R.id.tv_share)
        resources?.let { tvShare.setText(it.getString(R.string.just_share)) }

        // Share button
        findViewById<View>(R.id.btn_share).setOnClickListener {
            try {
                val shareText = if (!Utils.isProbablyArabic(reader ?: "")) {
                    String.format("%s %s #NurMontage_app #قرآن_كريم ", surah, reader)
                } else {
                    String.format(" %s بصوت %s #تطبيق_NurMontage #قرآن_كريم", surah, reader)
                }

                val videoFile = File(Uri.parse(mUri).path ?: "")
                Intent(Intent.ACTION_SEND).apply {
                    putExtra("act", "ACT_SHARE")
                    putExtra(Intent.EXTRA_TITLE, "Send To")
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.nurmontage_app))
                    putExtra(
                        Intent.EXTRA_STREAM,
                        FileProvider.getUriForFile(
                            this@VideoViewActivity,
                            getString(R.string.file_provider),
                            videoFile
                        )
                    )
                    type = "video/mp4"
                    startActivity(Intent.createChooser(this, "Send To"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Back button
        findViewById<View>(R.id.btn_on_back).setOnClickListener { toStudio() }

        // Help button
        findViewById<TextCustumFont>(R.id.tv_help).setText(R.string.help)
        findViewById<View>(R.id.btn_help).setOnClickListener { help() }

        ratingSetup()
    }

    override fun onResume() {
        super.onResume()
        setSystemUiAppearance()
    }

    override fun onPause() {
        cancelDialog()
        super.onPause()
    }

    // ── System UI ──────────────────────────────────────────────────────

    private fun setSystemUiAppearance() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.clearFlags(0x00000400) // FLAG_KEEP_SCREEN_ON
        window.clearFlags(0x00000200) // FLAG_SHOW_WHEN_LOCKED
        setStatusBarColor(ViewCompat.MEASURED_STATE_MASK)
        setNavigationBarColor(ViewCompat.MEASURED_STATE_MASK)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false
    }

    // ── Navigation helpers ─────────────────────────────────────────────

    private fun toStudio() {
        Intent(this, EngineActivity::class.java).apply {
            putExtra(Common.TEMPLATE, idTemplate)
            startActivity(this)
        }
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
        finish()
    }

    @Suppress("unused")
    private fun toAbout() {
        startActivity(Intent(this, AboutActivity::class.java))
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }

    @Suppress("unused")
    private fun toPro() {
        startActivity(Intent(this, ProVersionActivity::class.java))
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }

    // ── External links ─────────────────────────────────────────────────

    private fun help() {
        try {
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://chat.whatsapp.com/F0kqOjZS1VuBAvoiOG4XEZ")
                setPackage("com.whatsapp")
                startActivity(this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun installTuffah() {
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$TUFFAH_PACKAGE")
            ).apply {
                setPackage("com.android.vending")
                addFlags(0x58000000.toInt())
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

    // ── Rating ─────────────────────────────────────────────────────────

    private fun ratingSetup() {
        try {
            // TODO: RatingBottomSheetDialog needs to be converted to Kotlin
            if (!RatingBottomSheetDialog.shouldShowRatingDialog(this)) return
            if (resources == null) return
            if (trackerSession() < 4) return
            dialogRate()
        } catch (_: Exception) {
        }
    }

    fun trackerSession(): Int {
        val prefs = getSharedPreferences("ActPreference", MODE_PRIVATE)
        val count = prefs.getInt("session_count", 0) + 1
        prefs.edit().putInt("session_count", count).apply()
        return count
    }

    fun resetTrackerSession() {
        getSharedPreferences("ActPreference", MODE_PRIVATE)
            .edit()
            .putInt("session_count", 0)
            .apply()
    }

    // ── Dialogs ────────────────────────────────────────────────────────

    private fun cancelDialog() {
        dialog?.let {
            if (it.isShowing) it.dismiss()
        }
        dialog = null
    }

    private fun openPlayStoreForRating() {
        val packageName = packageName
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$packageName")
            ).apply {
                setPackage("com.android.vending")
                addFlags(0x58000000.toInt())
            }
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
                    )
                )
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(this, "Unable to open app store or browser.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun dialogRate() {
        Dialog(this).apply {
            dialog = this
            setCancelable(false)
            requestWindowFeature(1)
            window?.setLayout(-1, -2)
            window?.setBackgroundDrawable(ColorDrawable(0))

            val view = LayoutInflater.from(this@VideoViewActivity)
                .inflate(R.layout.layout_dialog_rate, null as ViewGroup?)
            setContentView(view)

            view.findViewById<TextCustumFontBold>(R.id.tv_tittle)
                .setText(R.string.how_many_stars)

            view.findViewById<ButtonCustumFont>(R.id.btn_rate).apply {
                setText(R.string.rate_now)
                setOnClickListener {
                    try {
                        openPlayStoreForRating()
                        RatingBottomSheetDialog.setNeverAskAgain(this@VideoViewActivity, true)
                        cancelDialog()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            view.findViewById<ButtonCustumFont>(R.id.btn_rate_not_now).apply {
                setText(R.string.later)
                setOnClickListener {
                    resetTrackerSession()
                    cancelDialog()
                }
            }

            show()
        }
    }

    companion object {
        private const val TUFFAH_PACKAGE = "hazem.tuffah.quranaudio"
    }
}
