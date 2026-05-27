package hazem.nurmontage.videoquran.entity_timeline

import hazem.nurmontage.videoquran.model.EntityQuranTemplate

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

    override fun getType(): EntityType = EntityType.QURAN

    fun fromTemplate(tmpl: EntityQuranTemplate) {
        template = tmpl
        ayaNumber = tmpl.number
        ayaText = tmpl.aya ?: ""
        completeAyaText = tmpl.complete_aya ?: ""
    }
}
