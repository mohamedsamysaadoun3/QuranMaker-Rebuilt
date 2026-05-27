package hazem.nurmontage.videoquran.Utils

import android.content.Context
import android.content.Intent
import android.provider.Settings

/**
 * Opens app settings screen.
 */
object AppSettingsHelper {

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
