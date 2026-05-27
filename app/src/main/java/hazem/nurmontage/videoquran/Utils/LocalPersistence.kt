package hazem.nurmontage.videoquran.Utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import hazem.nurmontage.videoquran.model.Template

/**
 * Utility class for persisting template objects to/from SharedPreferences using Gson serialization.
 *
 * Templates are stored in a shared preferences file named "MTemplate" as JSON strings,
 * keyed by a string identifier.
 */
object LocalPersistence {

    private const val PREFS_NAME = "MTemplate"

    private val gson: Gson by lazy { GsonBuilder().create() }

    private fun Context.templatePrefs() =
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Reads a [Template] object from SharedPreferences, identified by [key].
     *
     * @param context Android context used to access SharedPreferences.
     * @param key     The preference key under which the template JSON is stored.
     * @return The deserialized [Template], or `null` if reading or parsing fails.
     */
    fun readObjectFromFile(context: Context, key: String): Template? {
        return try {
            val json = context.templatePrefs().getString(key, null)
            json?.let { gson.fromJson(it, Template::class.java) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Writes a template object to SharedPreferences under [newKey],
     * and removes the old entry at [oldKey].
     *
     * This is useful when renaming/moving a template — the old key is cleared
     * and the object is stored under the new key.
     *
     * @param context Android context used to access SharedPreferences.
     * @param obj     The template object to serialize and store.
     * @param oldKey  The previous key to remove.
     * @param newKey  The new key under which to store the serialized template.
     */
    fun writeTemplate(context: Context, obj: Any, oldKey: String, newKey: String) {
        try {
            context.templatePrefs().edit()
                .remove(oldKey)
                .putString(newKey, gson.toJson(obj))
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Duplicates a template object by writing it under [key] without removing any existing entry.
     *
     * @param context Android context used to access SharedPreferences.
     * @param obj     The template object to serialize and store.
     * @param key     The preference key under which to store the serialized template.
     */
    fun duplicateTemplate(context: Context, obj: Any, key: String) {
        try {
            context.templatePrefs().edit()
                .putString(key, gson.toJson(obj))
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Deletes a template entry from SharedPreferences.
     *
     * @param context Android context used to access SharedPreferences.
     * @param key     The preference key of the template to remove.
     */
    fun deleteTemplate(context: Context, key: String) {
        try {
            context.templatePrefs().edit()
                .remove(key)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
