package hazem.nurmontage.videoquran.Utils

import android.content.Context
import android.content.SharedPreferences

/**
 * SharedPreferences for Quran search/reciter state.
 */
object QuranPreference {

    private const val PREFS_NAME = "quran_prefs"
    private const val KEY_LAST_SURAH = "last_surah"
    private const val KEY_LAST_AYA = "last_aya"
    private const val KEY_LAST_READER = "last_reader"
    private const val KEY_LAST_TRANSLATION_LANG = "last_translation_lang"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveLastPosition(context: Context, surah: Int, aya: Int) {
        getPrefs(context).edit().putInt(KEY_LAST_SURAH, surah).putInt(KEY_LAST_AYA, aya).apply()
    }

    fun getLastSurah(context: Context): Int = getPrefs(context).getInt(KEY_LAST_SURAH, 1)
    fun getLastAya(context: Context): Int = getPrefs(context).getInt(KEY_LAST_AYA, 1)

    fun saveLastReader(context: Context, reader: String) {
        getPrefs(context).edit().putString(KEY_LAST_READER, reader).apply()
    }

    fun getLastReader(context: Context): String = getPrefs(context).getString(KEY_LAST_READER, "") ?: ""

    fun saveLastTranslationLang(context: Context, lang: String) {
        getPrefs(context).edit().putString(KEY_LAST_TRANSLATION_LANG, lang).apply()
    }

    fun getLastTranslationLang(context: Context): String = getPrefs(context).getString(KEY_LAST_TRANSLATION_LANG, "ar") ?: "ar"
}
