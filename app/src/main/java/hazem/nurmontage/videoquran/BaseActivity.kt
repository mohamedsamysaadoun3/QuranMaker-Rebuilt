package hazem.nurmontage.videoquran

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import hazem.nurmontage.videoquran.Utils.LocaleHelper
import hazem.nurmontage.videoquran.Utils.MyVibrationHelper

/**
 * Base activity class for the entire application.
 * Handles common functionality like status bar color, navigation bar color,
 * locale management, window insets, and vibration feedback.
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

    /**
     * Hide system bars (status bar + navigation bar) for immersive mode.
     */
    protected open fun hideSystemBars() {
        val window = window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.hide(WindowInsets.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )
        }
    }

    /**
     * Acquire a wake lock to keep the screen on.
     * Named to match the original Java Base class method.
     */
    protected fun wakeLockAquire() {
        wakeLockAcquire()
    }

    /**
     * Acquire a wake lock to keep the screen on.
     */
    protected open fun wakeLockAcquire() {
        try {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } catch (_: Exception) {
        }
    }

    /**
     * Play a short vibration to provide haptic feedback.
     */
    protected open fun playVibration() {
        MyVibrationHelper.vibrate(this, 50)
    }
}
