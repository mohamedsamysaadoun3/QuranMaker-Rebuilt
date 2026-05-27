package hazem.nurmontage.videoquran

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import hazem.nurmontage.videoquran.databinding.ActivityFullscreenBinding

/**
 * Splash screen activity – the entry point of the application.
 * Simply installs the splash screen and inflates the fullscreen layout.
 * The actual routing logic is handled by [FullscreenActivity].
 */
class SplashscreenActivity : BaseActivity() {

    private lateinit var binding: ActivityFullscreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivityFullscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
