package hazem.nurmontage.videoquran.entity_timeline

import hazem.nurmontage.videoquran.model.EntityQuranTemplate
import hazem.nurmontage.videoquran.model.QuranEntity

/**
 * Quran ayah timeline block entity.
 * Represents a single ayah on the timeline with start/end times.
 */
class EntityQuranTimeline : Entity() {

    var template: EntityQuranTemplate = EntityQuranTemplate()
    var ayaNumber: Int = 0
    var surahNumber: Int = 0
    var surahName: String = ""
    var ayaText: String = ""
    var completeAyaText: String = ""

    // Seconds per pixel on the timeline (used by effect fragments)
    var secondInScreen: Float = 1f

    // QuranEntity view model (used by effect fragments for animations)
    var quranEntity: QuranEntity = QuranEntity()

    override fun getType(): EntityType = EntityType.QURAN

    fun fromTemplate(tmpl: EntityQuranTemplate) {
        template = tmpl
        ayaNumber = tmpl.number
        ayaText = tmpl.aya ?: ""
        completeAyaText = tmpl.complete_aya ?: ""
    }
}
