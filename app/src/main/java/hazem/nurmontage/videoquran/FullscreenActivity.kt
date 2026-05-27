package hazem.nurmontage.videoquran

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import hazem.nurmontage.videoquran.Utils.LocalPersistence
import hazem.nurmontage.videoquran.common.Common
import hazem.nurmontage.videoquran.databinding.ActivityFullscreenBinding

/**
 * Fullscreen splash activity that acts as the app's router.
 *
 * After displaying the splash screen for a brief period it decides which
 * activity to launch next:
 * - If launched with `from_setting` extra → [SeettingActivity]
 * - If no cached template exists and shared prefs contain templates → [WorkUserActivity]
 * - Otherwise → [EngineActivity]
 */
class FullscreenActivity : BaseActivity() {

    private lateinit var binding: ActivityFullscreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivityFullscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarColor(Color.WHITE)
        setNavigationBarColor(Color.WHITE)

        // Explicitly set light appearance for status/navigation bars
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = true
        insetsController.isAppearanceLightNavigationBars = true

        val allPrefs = getSharedPreferences("MTemplate", MODE_PRIVATE).all

        Handler(Looper.getMainLooper()).postDelayed({
            routeToNextActivity(allPrefs)
        }, SPLASH_DELAY_MS)
    }

    private fun routeToNextActivity(allPrefs: Map<String, *>?) {
        // If navigated from settings, go directly to settings
        if (intent != null && intent.getBooleanExtra(EXTRA_FROM_SETTING, false)) {
            startActivity(Intent(this, SeettingActivity::class.java))
            finish()
            return
        }

        // Determine routing based on whether a cached template exists
        val hasCachedTemplate = LocalPersistence.readObjectFromFile(
            this, Common.TEMPLATE_TMP
        ) != null

        val nextIntent = if (hasCachedTemplate) {
            // Has a cached template in progress → go to engine directly
            Intent(this, EngineActivity::class.java)
        } else {
            // No cached template → go to project list (home screen)
            Intent(this, WorkUserActivity::class.java)
        }

        startActivity(nextIntent)
        finish()
    }

    companion object {
        private const val SPLASH_DELAY_MS = 1200L
        private const val EXTRA_FROM_SETTING = "from_setting"
    }
}
