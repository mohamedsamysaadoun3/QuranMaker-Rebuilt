package hazem.nurmontage.videoquran.fragment

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.views.ButtonCustumFont
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * Bottom sheet dialog prompting the user to rate the app on Google Play.
 * Converted from original Java RatingBottomSheetDialog (91 lines).
 */
class RatingBottomSheetDialog() : BottomSheetDialogFragment() {
    private var dialogView: android.view.View? = null

    private var res: Resources? = null

    constructor(resources: Resources) : this() {
        this.res = resources
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.layout_dialog_rate, container, false)

        if (res == null) return view

        val rateButton = view.findViewById<ButtonCustumFont>(R.id.rateButton)
        val laterButton = view.findViewById<ButtonCustumFont>(R.id.laterButton)
        val neverButton = view.findViewById<ButtonCustumFont>(R.id.dialog_no)

        rateButton.text = res!!.getString(R.string.rate_now)
        laterButton.text = res!!.getString(R.string.later)
        neverButton.text = res!!.getString(R.string.no_thanks)

        val tvTitle = view.findViewById<TextCustumFont>(R.id.tv_tittle)
        val tvSubtitle = view.findViewById<TextCustumFont>(R.id.tv_subtittle)
        tvTitle.text = res!!.getString(R.string.enjoying_the_app)
        tvSubtitle.text = res!!.getString(R.string.moment_to_rate)

        rateButton.setOnClickListener {
            openPlayStore(requireContext())
            context?.let { ctx -> setNeverAskAgain(ctx, true) }
            dismiss()
        }

        laterButton.setOnClickListener {
            dismiss()
        }

        neverButton.setOnClickListener {
            context?.let { ctx -> setNeverAskAgain(ctx, true) }
            dismiss()
        }

        return view
    }

    private fun openPlayStore(context: Context?) {
        context ?: return
        val packageName = context.packageName
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            )
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
            )
        }
    }

    companion object {
        private const val KEY_NEVER_ASK_AGAIN = "never_ask_again_new"
        private const val PREFS_NAME = "app_prefs_new_mars"

        @JvmStatic
        fun setNeverAskAgain(context: Context, value: Boolean) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_NEVER_ASK_AGAIN, value)
                .apply()
        }

        @JvmStatic
        fun shouldShowRatingDialog(context: Context): Boolean {
            return !context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_NEVER_ASK_AGAIN, false)
        }
    }
}
