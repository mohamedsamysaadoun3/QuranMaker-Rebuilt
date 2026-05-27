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

    fun onAttach(context: Context): Context =
        setLocale(context, getPersistedData(context, getLanguage(context)))

    fun getLanguage(context: Context): String =
        getPersistedData(context, "en")

    fun setLocale(language: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
    }

    fun setLocale(context: Context, language: String): Context {
        persist(context, language)
        return updateResources(context, language)
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

    fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(locale))
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun updateResourcesLegacy(context: Context, language: String): Context {
        val locale = Locale(language)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(locale))
        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        val newContext = context.createConfigurationContext(config)
        if (Build.VERSION.SDK_INT <= 24) {
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
        }
        return newContext
    }
}
