package hazem.nurmontage.videoquran.Utils

import android.content.Context

/**
 * Utility class for managing billing/subscription preferences stored in SharedPreferences.
 *
 * Tracks whether the user has an active subscription. All preferences are stored
 * in a shared preferences file named "BillingPrefs".
 */
object BillingPreferences {

    private const val PREF_NAME = "BillingPrefs"
    private const val KEY_IS_SUBSCRIBED = "isSubscribed"

    private fun Context.billingPrefs() =
        getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Saves the subscription status.
     *
     * @param context     Android context used to access SharedPreferences.
     * @param isSubscribed `true` if the user has an active subscription, `false` otherwise.
     */
    fun saveSubscriptionStatus(context: Context, isSubscribed: Boolean) {
        context.billingPrefs().edit()
            .putBoolean(KEY_IS_SUBSCRIBED, isSubscribed)
            .apply()
    }

    /**
     * Returns `true` if the user currently has an active subscription (default false).
     *
     * @param context Android context used to access SharedPreferences.
     */
    fun isSubscribed(context: Context): Boolean =
        context.billingPrefs().getBoolean(KEY_IS_SUBSCRIBED, false)

    /**
     * Resets the subscription status to unsubscribed (false).
     *
     * This is typically called when the user's subscription expires or is cancelled.
     *
     * @param context Android context used to access SharedPreferences.
     */
    fun saveSubscribeAllItemValueTofalse(context: Context) {
        context.billingPrefs().edit()
            .putBoolean(KEY_IS_SUBSCRIBED, false)
            .apply()
    }
}
