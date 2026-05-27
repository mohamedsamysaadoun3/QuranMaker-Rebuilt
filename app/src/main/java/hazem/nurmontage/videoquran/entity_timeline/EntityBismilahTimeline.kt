package hazem.nurmontage.videoquran.entity_timeline

import hazem.nurmontage.videoquran.model.EntityBismilahTemplate
import hazem.nurmontage.videoquran.model.QuranEntity

/**
 * Bismilah timeline block entity.
 * Represents a Bismilah block on the timeline.
 */
class EntityBismilahTimeline : Entity() {

    var template: EntityBismilahTemplate = EntityBismilahTemplate()
    var bismilahText: String = "بسم الله الرحمن الرحيم"

    // Seconds per pixel on the timeline (used by effect fragments)
    var secondInScreen: Float = 1f

    // QuranEntity reference (used by effect fragments for animations)
    // For Bismilah, this wraps the bismilah-specific entity view
    var quranEntity: QuranEntity = QuranEntity()

    override fun getType(): EntityType = EntityType.BISMILAH

    fun fromTemplate(tmpl: EntityBismilahTemplate) {
        template = tmpl
    }
}
