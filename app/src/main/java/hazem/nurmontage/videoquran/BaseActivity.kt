package hazem.nurmontage.videoquran

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import hazem.nurmontage.videoquran.Utils.LocaleHelper

/**
 * Base activity class for the entire application.
 * Handles common functionality like status bar color, navigation bar color,
 * locale management, and window insets.
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EdgeToEdge.enable(this)
    }

    protected fun setStatusBarColor(color: Int) {
        window.statusBarColor = color
        val isLight = isColorLight(color)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = isLight
    }

    protected fun setNavigationBarColor(color: Int) {
        window.navigationBarColor = color
        val isLight = isColorLight(color)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightNavigationBars = isLight
    }

    private fun isColorLight(color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness < 0.5
    }
}
