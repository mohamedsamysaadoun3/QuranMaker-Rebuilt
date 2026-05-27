package hazem.nurmontage.videoquran.Utils

import android.content.Context

/**
 * App version name/code utility.
 */
object AppUtils {

    fun getVersionName(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: ""
        } catch (_: Exception) { "" }
    }

    fun getVersionCode(context: Context): Long {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.longVersionCode
        } catch (_: Exception) { 0L }
    }

    /**
     * Alias matching the original Java API name.
     * Returns the app version name string, or "1.6" as fallback.
     */
    fun getAppVersionName(context: Context): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.6"
        } catch (_: Exception) { "1.6" }
    }
}
