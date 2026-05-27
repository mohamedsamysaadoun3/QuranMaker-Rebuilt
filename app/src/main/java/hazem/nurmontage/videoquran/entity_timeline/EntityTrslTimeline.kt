package hazem.nurmontage.videoquran.entity_timeline

import hazem.nurmontage.videoquran.model.EntityTranslationTemplate

class EntityTrslTimeline : Entity() {

    var template: EntityTranslationTemplate = EntityTranslationTemplate()
    var translationText: String = ""
    var completeTranslation: String = ""

    override fun getType(): EntityType = EntityType.TRANSLATION

    fun fromTemplate(tmpl: EntityTranslationTemplate) {
        template = tmpl
        translationText = tmpl.translation ?: ""
        completeTranslation = tmpl.translationComplete ?: ""
    }
}
