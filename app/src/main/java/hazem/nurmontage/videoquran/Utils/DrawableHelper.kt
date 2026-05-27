package hazem.nurmontage.videoquran.Utils

import android.content.Context

/**
 * Maps drawable icon/background names to resource IDs.
 * Used by templates to reference drawable resources.
 */
object DrawableHelper {

    private val iconMap = mapOf(
        "hafes" to "hafes_icon",
        "amiri" to "amiri_icon",
        "taha" to "taha_icon",
        "shamerli" to "shamerli_icon",
        "warach" to "warach_icon",
        "nour_hoda" to "nour_hoda_icon",
        "nour_hode" to "nour_hoda_icon",
    )

    fun getDrawableResId(context: Context, name: String): Int {
        return context.resources.getIdentifier(name, "drawable", context.packageName)
    }

    fun getIconDrawableName(iconKey: String): String {
        return iconMap[iconKey] ?: "hafes_icon"
    }

    fun getBackgroundDrawableName(index: Int): String {
        return if (index in 1..38) "bg_$index" else "bg_1"
    }

    /**
     * Get drawable resource ID by icon name (e.g., "hafes" -> R.drawable.hafes_icon).
     * Used by IconQuranAdabters. Requires a Context for resource lookup.
     */
    fun getIDDrawableIconByName(context: Context, name: String): Int {
        val drawableName = getIconDrawableName(name)
        return getDrawableResId(context, drawableName)
    }
}
