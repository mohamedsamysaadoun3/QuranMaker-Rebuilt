package hazem.nurmontage.videoquran.entity_timeline

import hazem.nurmontage.videoquran.model.EntityBismilahTemplate

/**
 * Bismilah timeline block entity.
 * Represents a Bismilah block on the timeline.
 */
class EntityBismilahTimeline : Entity() {

    var template: EntityBismilahTemplate = EntityBismilahTemplate()
    var bismilahText: String = "بسم الله الرحمن الرحيم"

    override fun getType(): EntityType = EntityType.BISMILAH

    fun fromTemplate(tmpl: EntityBismilahTemplate) {
        template = tmpl
    }
}
