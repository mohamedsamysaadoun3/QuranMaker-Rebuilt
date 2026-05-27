package hazem.nurmontage.videoquran.Utils

import android.content.Context
import android.content.SharedPreferences

/**
 * SharedPreferences for Quran search/reciter state.
 *
 * Uses the original preference file name "QuranPrefs_" to maintain
 * backward compatibility with existing user data.
 */
object QuranPreference {

    private const val PREFS_NAME = "QuranPrefs_"
    private const val KEY_FROM = "from"
    private const val KEY_SURAH = "surah"
    private const val KEY_TO = "to"
    private const val KEY_SEARCH = "search"
    private const val KEY_NAME_READER = "name_reader_"
    private const val KEY_TRANSLATION = "translation_select"
    private const val KEY_LAST_TRANSLATION_LANG = "last_translation_lang"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Saves the search result position (surah, from-aya, to-aya) and search text.
     */
    fun savePreferencesSearch(context: Context, surah: Int, from: Int, to: Int, searchText: String) {
        getPrefs(context).edit()
            .putInt(KEY_FROM, from)
            .putInt(KEY_TO, to)
            .putInt(KEY_SURAH, surah)
            .putString(KEY_SEARCH, searchText)
            .apply()
    }

    /**
     * Saves the search result position (surah, aya) without search text.
     */
    fun savePreferencesSearch(context: Context, surah: Int, aya: Int) {
        getPrefs(context).edit()
            .putInt(KEY_FROM, aya)
            .putInt(KEY_TO, aya)
            .putInt(KEY_SURAH, surah)
            .apply()
    }

    /**
     * Saves the last search text.
     */
    fun saveLastSearch(context: Context, searchText: String) {
        getPrefs(context).edit()
            .putString(KEY_SEARCH, searchText)
            .apply()
    }

    /**
     * Returns the last saved search text, or empty string if none.
     */
    fun getLastSearch(context: Context): String =
        getPrefs(context).getString(KEY_SEARCH, "") ?: ""

    fun getSurah(context: Context): Int = getPrefs(context).getInt(KEY_SURAH, 0)
    fun getFrom(context: Context): Int = getPrefs(context).getInt(KEY_FROM, 0)
    fun getTo(context: Context): Int = getPrefs(context).getInt(KEY_TO, 0)
    fun getNameReader(context: Context): Int = getPrefs(context).getInt(KEY_NAME_READER, 0)
    fun getTranslation(context: Context): Int = getPrefs(context).getInt(KEY_TRANSLATION, 0)

    fun savePreferences(context: Context, surah: Int, from: Int, to: Int, nameReader: Int, translation: Int) {
        getPrefs(context).edit()
            .putInt(KEY_FROM, from)
            .putInt(KEY_TO, to)
            .putInt(KEY_SURAH, surah)
            .putInt(KEY_NAME_READER, nameReader)
            .putInt(KEY_TRANSLATION, translation)
            .apply()
    }

    fun saveLastTranslationLang(context: Context, lang: String) {
        getPrefs(context).edit()
            .putString(KEY_LAST_TRANSLATION_LANG, lang)
            .apply()
    }

    fun getLastTranslationLang(context: Context): String =
        getPrefs(context).getString(KEY_LAST_TRANSLATION_LANG, "ar") ?: "ar"
}
