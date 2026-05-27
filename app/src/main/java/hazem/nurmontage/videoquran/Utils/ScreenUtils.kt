package hazem.nurmontage.videoquran.Utils

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.WindowMetrics

/**
 * Screen width/height detection with API 30+ support.
 */
object ScreenUtils {

    fun getScreenWidth(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics: WindowMetrics = wm.currentWindowMetrics
            metrics.bounds.width()
        } else {
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getMetrics(metrics)
            metrics.widthPixels
        }
    }

    fun getScreenHeight(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics: WindowMetrics = wm.currentWindowMetrics
            metrics.bounds.height()
        } else {
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getMetrics(metrics)
            metrics.heightPixels
        }
    }

    fun getScreenWidthPx(context: Context): Int = getScreenWidth(context)
    fun getScreenHeightPx(context: Context): Int = getScreenHeight(context)

    fun dpToPx(context: Context, dp: Float): Float =
        dp * context.resources.displayMetrics.density

    fun pxToDp(context: Context, px: Float): Float =
        px / context.resources.displayMetrics.density
}
