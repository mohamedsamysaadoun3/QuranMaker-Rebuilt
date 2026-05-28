package hazem.nurmontage.videoquran.Utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {

    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"
    private const val PREFS_NAME = "ActPreference"
    private const val KEY_USER_CHOICE = "userIsChoice"

    fun onAttach(context: Context): Context {
        val language = getPersistedData(context, "en")
        return updateResourcesLegacy(context, language)
    }

    fun getLanguage(context: Context): String =
        getPersistedData(context, "en")

    /**
     * Call this when user explicitly changes language.
     * Uses AppCompatDelegate.setApplicationLocales() for the modern per-app language API.
     */
    fun setLocale(language: String) {
        val locale = Locale(language)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(locale))
    }

    /**
     * Apply locale to context and persist the choice.
     * Called from ChoiceLangActivity when user confirms language change.
     */
    fun setLocale(context: Context, language: String): Context {
        persist(context, language)
        setLocale(language)
        return updateResourcesLegacy(context, language)
    }

    fun getPersistedData(context: Context, defaultLanguage: String): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(SELECTED_LANGUAGE, defaultLanguage) ?: defaultLanguage

    fun persist(context: Context, language: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(SELECTED_LANGUAGE, language)
            .apply()
    }

    fun userIsChoice(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_USER_CHOICE, true)
            .apply()
    }

    fun getUserIsChoice(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_USER_CHOICE, false)

    /**
     * Apply locale to context configuration for legacy Android versions.
     * Does NOT call AppCompatDelegate.setApplicationLocales() to avoid double-wrapping
     * with the attachBaseContext flow.
     */
    private fun updateResourcesLegacy(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val newContext = context.createConfigurationContext(config)
        if (Build.VERSION.SDK_INT <= 24) {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
        return newContext
    }
}
