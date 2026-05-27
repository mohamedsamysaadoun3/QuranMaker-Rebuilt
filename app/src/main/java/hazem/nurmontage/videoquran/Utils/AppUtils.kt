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
}
