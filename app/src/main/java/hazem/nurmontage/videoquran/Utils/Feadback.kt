package hazem.nurmontage.videoquran.Utils

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Bug report / feedback email sender.
 */
object Feadback {

    private const val SUPPORT_EMAIL = "hazemourari08@gmail.com"

    fun sendFeedback(context: Context, subject: String = "NurMontage Feedback", body: String = "") {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        context.startActivity(Intent.createChooser(intent, "Send Feedback"))
    }

    fun sendBugReport(context: Context, errorDetails: String = "") {
        val subject = "NurMontage Bug Report"
        val body = "App Version: ${AppUtils.getVersionName(context)}\n\nError Details:\n$errorDetails"
        sendFeedback(context, subject, body)
    }

    fun sendRenderStart(context: android.content.Context) { /* TODO: Phase 7 */ }
    fun sendRenderEnd(context: android.content.Context) { /* TODO: Phase 7 */ }
}
