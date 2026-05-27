package hazem.nurmontage.videoquran

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Pair
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.Insets
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import hazem.nurmontage.videoquran.Utils.AppUtils
import hazem.nurmontage.videoquran.Utils.LocaleHelper
import hazem.nurmontage.videoquran.Utils.ScreenUtils
import hazem.nurmontage.videoquran.adabter.AboutAdabters
import hazem.nurmontage.videoquran.databinding.ActivityAboutBinding

/**
 * About screen that displays information about the app using a RecyclerView.
 *
 * Shows a list of "about" items (text + optional images) describing the app,
 * its philosophy, and how to get help. Includes a WhatsApp help link.
 */
class AboutActivity : BaseActivity() {

    private lateinit var binding: ActivityAboutBinding

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // Dark status/navigation bar
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

        if (resources == null) {
            finish()
            return
        }

        init()
    }

    /**
     * Open WhatsApp help group.
     */
    private fun help() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(WHATSAPP_HELP_URL)
                setPackage("com.whatsapp")
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Build and populate the about list with items.
     */
    private fun init() {
        // Back button
        binding.btnOnBack.setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }

        // Help button
        findViewById<View>(R.id.btn_help).setOnClickListener {
            help()
        }

        (findViewById<View>(R.id.tv_help) as hazem.nurmontage.videoquran.views.TextCustumFont).text = getString(R.string.help)

        // Determine text gravity based on language
        val gravity = if (LocaleHelper.getLanguage(this) == "ar") 5 else GravityCompat.START

        // Build about items list
        val aboutItems = ArrayList<AboutAdabters.ModelAbout>()

        aboutItems.add(AboutAdabters.ModelAbout(19, Pair("<font color=#F8B195>${getString(R.string.about_free_site)}</font>", gravity), R.drawable.about_site_video))
        aboutItems.add(AboutAdabters.ModelAbout(14, Pair(getString(R.string.about_free_site_desc), gravity)))
        aboutItems.add(AboutAdabters.ModelAbout(14, Pair("\n", gravity)))

        aboutItems.add(AboutAdabters.ModelAbout(19, Pair("<font color=#F8B195>${getString(R.string.about_free_app)}</font>", gravity), R.drawable.about_best_app))
        aboutItems.add(AboutAdabters.ModelAbout(14, Pair("\n", gravity)))

        aboutItems.add(AboutAdabters.ModelAbout(19, Pair("<font color=#F8B195>${getString(R.string.about_dont_subscribe)}</font>", gravity), R.drawable.about_money))
        aboutItems.add(AboutAdabters.ModelAbout(14, Pair("<font color='#ffffff'>${getString(R.string.about_dont_subscribe_why)}</font>", gravity)))
        aboutItems.add(AboutAdabters.ModelAbout(14, Pair("\n", gravity)))

        aboutItems.add(AboutAdabters.ModelAbout(19, Pair("<font color=#F8B195>${getString(R.string.this_begeing_idea)}</font>", gravity), R.drawable.about_hazem))
        aboutItems.add(AboutAdabters.ModelAbout(14, Pair("<font color='#ffffff'>${getString(R.string.this_begeing_idea_decp)}</font>", gravity)))
        aboutItems.add(AboutAdabters.ModelAbout(14, Pair("\n", gravity)))

        aboutItems.add(AboutAdabters.ModelAbout(19, Pair("<font color=#F8B195>${getString(R.string.about_help_tittle)}</font>", gravity), R.drawable.about_help))
        aboutItems.add(AboutAdabters.ModelAbout(14, Pair("<font color='#ffffff'>${getString(R.string.about_help_body)}</font>", gravity)))
        aboutItems.add(AboutAdabters.ModelAbout(14, Pair("\n", gravity)))

        aboutItems.add(AboutAdabters.ModelAbout(19, Pair("<font color=#F8B195>${getString(R.string.nurmontage_means)}</font>", gravity), R.drawable.nurmontage_means))
        aboutItems.add(AboutAdabters.ModelAbout(14, Pair("<font color='#ffffff'>${getString(R.string.nurmontage_means_descrp)}</font>", gravity)))
        aboutItems.add(AboutAdabters.ModelAbout(14, Pair("\n", gravity)))

        aboutItems.add(AboutAdabters.ModelAbout(19, Pair("<font color=#F8B195>${getString(R.string.help_me_help_you)}</font>", gravity), R.drawable.about_help_me_help_you))
        aboutItems.add(AboutAdabters.ModelAbout(14, Pair("<font color='#ffffff'>${getString(R.string.help_me_help_you_descrp)}</font>", gravity)))
        aboutItems.add(AboutAdabters.ModelAbout(14, Pair("\n", gravity)))

        aboutItems.add(AboutAdabters.ModelAbout(0, Pair("", gravity), R.drawable.signature_hazem))

        // Set up RecyclerView
        binding.rv.setHasFixedSize(true)
        binding.rv.layoutManager = LinearLayoutManager(this)
        binding.rv.adapter = AboutAdabters(
            this,
            AppUtils.getVersionName(this),
            aboutItems,
            ScreenUtils.getScreenWidth(this),
            (ScreenUtils.getScreenHeight(this) * 0.3f).toInt()
        )
    }

    companion object {
        private const val WHATSAPP_HELP_URL =
            "https://chat.whatsapp.com/DDdUegENpg83easzYDba2K?mode=wwt"
    }
}
