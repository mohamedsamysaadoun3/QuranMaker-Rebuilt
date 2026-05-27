package hazem.nurmontage.videoquran.Utils

import android.content.Context

/**
 * Utility class for managing application-wide preferences stored in SharedPreferences.
 *
 * Stores various boolean flags, integer values, and UI state such as:
 * - First-run detection
 * - About screen visited state
 * - Copyright acknowledgement
 * - Scroll position
 * - Hint visibility for crop/scale
 * - Selected Quran icon index
 * - Bismilah inclusion flag
 *
 * All preferences are stored in a shared preferences file named "MyPrefs".
 *
 * Note: The class name retains the original typo "Prefereces" (instead of "Preferences")
 * to maintain backward compatibility with existing preference data.
 */
object MyPrefereces {

    private const val PREFS_NAME = "MyPrefs"

    private const val FIRST_RUN_KEY = "firstRun"
    private const val IS_VU_ABOUT = "is_about"
    private const val IS_VU_COPYRIGHT = "is_vu_copyright"
    private const val SCROLL_X = "scroll_view_x"
    private const val HINT_CROP_SCALE = "hint_crop_scale"
    private const val ICON_QURAN = "icon_quran"
    private const val INCLUDE_BISMILAH = "IncludeBismilah"

    private fun Context.myPrefs() =
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Copyright ────────────────────────────────────────────────────────

    /** Returns `true` if the user has acknowledged the copyright notice. */
    fun isCopyRight(context: Context): Boolean =
        context.myPrefs().getBoolean(IS_VU_COPYRIGHT, false)

    /** Marks the copyright notice as acknowledged. */
    fun putVuCopyRight(context: Context) {
        context.myPrefs().edit()
            .putBoolean(IS_VU_COPYRIGHT, true)
            .apply()
    }

    // ── Scroll Position ──────────────────────────────────────────────────

    /** Returns the last saved horizontal scroll position (default 0). */
    fun getScrollX(context: Context): Int =
        context.myPrefs().getInt(SCROLL_X, 0)

    /** Persists the horizontal scroll position. */
    fun putScrollX(context: Context, scrollX: Int) {
        context.myPrefs().edit()
            .putInt(SCROLL_X, scrollX)
            .apply()
    }

    // ── Crop/Scale Hint ─────────────────────────────────────────────────

    /** Returns `true` if the crop/scale hint has been shown to the user. */
    fun isShowHint(context: Context): Boolean =
        context.myPrefs().getBoolean(HINT_CROP_SCALE, false)

    /** Marks the crop/scale hint as shown. */
    fun putShowHint(context: Context) {
        context.myPrefs().edit()
            .putBoolean(HINT_CROP_SCALE, true)
            .apply()
    }

    // ── Quran Icon Index ────────────────────────────────────────────────

    /** Returns the last selected Quran icon index (default 0). */
    fun getLastIconIndex(context: Context): Int =
        context.myPrefs().getInt(ICON_QURAN, 0)

    /** Persists the selected Quran icon index. */
    fun putIndexLastIcon(context: Context, index: Int) {
        context.myPrefs().edit()
            .putInt(ICON_QURAN, index)
            .apply()
    }

    // ── Bismilah Inclusion ──────────────────────────────────────────────

    /** Returns `true` if Bismilah should be included in output (default false). */
    fun isIncludeBismilah(context: Context): Boolean =
        context.myPrefs().getBoolean(INCLUDE_BISMILAH, false)

    /** Sets whether Bismilah should be included in output. */
    fun putIncludeBismilah(context: Context, include: Boolean) {
        context.myPrefs().edit()
            .putBoolean(INCLUDE_BISMILAH, include)
            .apply()
    }

    // ── About Screen ────────────────────────────────────────────────────

    /** Returns `true` if the user has already viewed the About screen. */
    fun isVueAbout(context: Context): Boolean =
        context.myPrefs().getBoolean(IS_VU_ABOUT, false)

    /** Marks the About screen as viewed. */
    fun putVueAbout(context: Context) {
        context.myPrefs().edit()
            .putBoolean(IS_VU_ABOUT, true)
            .apply()
    }

    // ── First Run ───────────────────────────────────────────────────────

    /** Returns `true` if this is the first time the app is launched (default true). */
    fun isFirstRun(context: Context): Boolean =
        context.myPrefs().getBoolean(FIRST_RUN_KEY, true)

    /** Marks the first-run as complete (sets the flag to false). */
    fun putFirstRun(context: Context) {
        context.myPrefs().edit()
            .putBoolean(FIRST_RUN_KEY, false)
            .apply()
    }
}
