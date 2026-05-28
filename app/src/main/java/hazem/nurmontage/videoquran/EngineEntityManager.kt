package hazem.nurmontage.videoquran

import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.VectorDrawable
import androidx.core.view.ViewCompat
import hazem.nurmontage.videoquran.Utils.DrawableHelper
import hazem.nurmontage.videoquran.Utils.UtilsFileLast
import hazem.nurmontage.videoquran.common.Common
import hazem.nurmontage.videoquran.constant.AyaTextPreset
import hazem.nurmontage.videoquran.constant.IpadType
import hazem.nurmontage.videoquran.constant.SurahNameStyle
import hazem.nurmontage.videoquran.entity_timeline.EntityBismilahTimeline
import hazem.nurmontage.videoquran.entity_timeline.EntityQuranTimeline
import hazem.nurmontage.videoquran.entity_timeline.EntityTrslTimeline
import hazem.nurmontage.videoquran.model.*
import hazem.nurmontage.videoquran.views.BlurredImageView
import java.lang.ref.WeakReference

/**
 * Manages entity CRUD (add/duplicate/split/delete Quran, Translation, 
 * Bismilah, Audio entities) and timeline entity operations for EngineActivity.
 */
class EngineEntityManager(
    private val activity: EngineActivity
) {

    // Timeline entity creation helpers
    fun addTimeLineQuran(quranEntity: QuranEntity, left: Float, right: Float): EntityQuranTimeline {
        val secondInScreen = activity.trackViewEntity.getSecondInScreen()
        val timeline = EntityQuranTimeline().apply {
            this.quranEntity = quranEntity
            startMs = left.toLong()
            endMs = right.toLong()
            this.secondInScreen = secondInScreen
        }
        activity.trackViewEntity.addQuran(timeline)
        return timeline
    }

    fun addTimeLineQuran(quranEntity: QuranEntity): EntityQuranTimeline {
        val xCursur = activity.trackViewEntity.getXCursur()
        val secondInScreen = activity.trackViewEntity.getSecondInScreen()
        val timeline = EntityQuranTimeline().apply {
            this.quranEntity = quranEntity
            startMs = xCursur.toLong()
            endMs = (xCursur + secondInScreen * 4f).toLong()
            this.secondInScreen = secondInScreen
        }
        activity.trackViewEntity.addQuran(timeline)
        return timeline
    }

    fun addTimeLineTrslQuran(translationQuranEntity: TranslationQuranEntity): EntityTrslTimeline {
        val xCursur = activity.trackViewEntity.getXCursur()
        val secondInScreen = activity.trackViewEntity.getSecondInScreen()
        val timeline = EntityTrslTimeline().apply {
            translationText = translationQuranEntity.text
            startMs = xCursur.toLong()
            endMs = (xCursur + secondInScreen * 4f).toLong()
        }
        activity.trackViewEntity.addTrslQuran(timeline)
        return timeline
    }

    fun addTimeLineTrslQuran(translationQuranEntity: TranslationQuranEntity, left: Float, right: Float): EntityTrslTimeline {
        val timeline = EntityTrslTimeline().apply {
            translationText = translationQuranEntity.text
            startMs = left.toLong()
            endMs = right.toLong()
        }
        activity.trackViewEntity.addTrslQuran(timeline)
        return timeline
    }

    fun addTimeLineBismilah(bismilahEntity: BismilahEntity, left: Float, right: Float): EntityBismilahTimeline {
        val secondInScreen = activity.trackViewEntity.getSecondInScreen()
        val timeline = EntityBismilahTimeline().apply {
            bismilahText = bismilahEntity.text
            startMs = left.toLong()
            endMs = right.toLong()
            this.secondInScreen = secondInScreen
        }
        activity.trackViewEntity.setBismilahTimeline(timeline)
        return timeline
    }

    fun addTimeLineBismilah(bismilahEntity: BismilahEntity): EntityBismilahTimeline {
        val secondInScreen = activity.trackViewEntity.getSecondInScreen()
        val timeline = EntityBismilahTimeline().apply {
            bismilahText = bismilahEntity.text
            val f = activity.trackViewEntity.getmIsi3adaTimeline()?.getRect()?.right?.toInt()?.toLong() ?: 0L
            startMs = f
            endMs = f + (secondInScreen * 4f).toLong()
            this.secondInScreen = secondInScreen
        }
        activity.trackViewEntity.setBismilahTimeline(timeline)
        return timeline
    }

    fun addTimeLineIsti3ada(bismilahEntity: BismilahEntity, left: Float, right: Float): EntityBismilahTimeline {
        val secondInScreen = activity.trackViewEntity.getSecondInScreen()
        val timeline = EntityBismilahTimeline().apply {
            bismilahText = bismilahEntity.text
            startMs = left.toLong()
            endMs = right.toLong()
            this.secondInScreen = secondInScreen
        }
        activity.trackViewEntity.setmIsi3adaTimeline(timeline)
        return timeline
    }

    fun addTimeLineIsti3ada(bismilahEntity: BismilahEntity): EntityBismilahTimeline {
        val secondInScreen = activity.trackViewEntity.getSecondInScreen()
        val timeline = EntityBismilahTimeline().apply {
            bismilahText = bismilahEntity.text
            startMs = 0L
            endMs = (secondInScreen * 4f).toLong()
            this.secondInScreen = secondInScreen
        }
        activity.trackViewEntity.setmIsi3adaTimeline(timeline)
        return timeline
    }

    // === ADD ENTITY METHODS ===

    fun addEntity(aya: String, completeAya: String, translation: String?, translationComplete: String?,
                 textLength: Int, ayaNumber: Int, icon: String, startWordIndex: Int, endWordIndex: Int) {
        val nameFont = if (activity.blurredImageView.getQuranEntities().isEmpty()) {
            Common.FONT_QURAN
        } else {
            activity.blurredImageView.getQuranEntities()[0].fontName ?: Common.FONT_QURAN
        }

        val quranEntity = QuranEntity()
        quranEntity.aya = aya
        quranEntity.completeAya = completeAya
        quranEntity.translation = translation
        quranEntity.fontName = nameFont
        quranEntity.icon = icon.ifBlank { "hafes" }
        quranEntity.textColor = -1
        quranEntity.entityIndex = ayaNumber
        quranEntity.entityTransition = Transition()
        val addTimeLineQuran = addTimeLineQuran(quranEntity)
        addTimeLineQuran.entityView = quranEntity
        activity.blurredImageView.addEntity(quranEntity)
    }

    fun addTranslationEntity(translation: String, ayaNumber: Int, isEnglish: Boolean) {
        val translationQuranEntity = TranslationQuranEntity().apply {
            text = translation
            fontName = "ReadexPro_Medium.ttf"
            textColor = -1
            entityIndex = ayaNumber
            entityTransition = Transition()
        }
        val addTimeLineTrslQuran = addTimeLineTrslQuran(translationQuranEntity)
        addTimeLineTrslQuran.entityView = translationQuranEntity
        activity.blurredImageView.addEntity(translationQuranEntity)
    }

    fun addEntityBissmilah(): Boolean {
        val bismilahEntity = BismilahEntity().apply {
            text = "بسم الله الرحمن الرحيم"
            fontName = "خط البسملة.ttf"
            textColor = -1
            entityTransition = Transition()
        }
        val addTimeLineBismilah = addTimeLineBismilah(bismilahEntity)
        addTimeLineBismilah.entityView = bismilahEntity
        activity.blurredImageView.setBismilahEntity(bismilahEntity)
        val quran = activity.trackViewEntity.getQuran()
        if (quran != null) {
            activity.trackViewEntity.translateToRightBismilah(addTimeLineBismilah)
        }
        return true
    }

    fun addEntityIste3adha(): Boolean {
        val bismilahEntity = BismilahEntity().apply {
            text = "أعوذ بالله من الشيطان الرجيم"
            fontName = "خط الاستعاذه.ttf"
            textColor = -1
            entityTransition = Transition()
        }
        val addTimeLineIsti3ada = addTimeLineIsti3ada(bismilahEntity)
        addTimeLineIsti3ada.entityView = bismilahEntity
        activity.blurredImageView.setIsti3adhaEntity(bismilahEntity)
        val quran = activity.trackViewEntity.getQuran()
        if (quran != null) {
            activity.trackViewEntity.translateToRightBismilah(addTimeLineIsti3ada)
        }
        return true
    }

    // === ENTITY OPERATIONS FROM TEMPLATE ===

    fun addEntityFromTemplate() {
        val template = activity.mTemplate ?: return

        for (entityQuranTemplate in template.quranEntityList) {
            addEntityFromQuranTemplate(entityQuranTemplate)
        }
        for (entityTranslationTemplate in template.translationTemplateList) {
            addEntityTrsl(entityTranslationTemplate)
        }
        template.entityIsti3adaTemplate?.let { addEntityIsti3ada(it) }
        template.entityBismilahTemplate?.let { addEntityBismilah(it) }
    }

    private fun addEntityFromQuranTemplate(tmpl: EntityQuranTemplate) {
        val nameFont = tmpl.name_font ?: "عثماني.otf"
        val icon = tmpl.icon ?: "hafes"
        val quranEntity = QuranEntity().apply {
            aya = tmpl.aya ?: ""
            completeAya = tmpl.complete_aya ?: ""
            fontName = nameFont
            this.icon = icon
            textColor = tmpl.color
            entityIndex = tmpl.number
            scale = tmpl.scale
            factorSize = tmpl.factor_size
            entityTransition = tmpl.transition ?: Transition()
        }
        val timeline = addTimeLineQuran(quranEntity, tmpl.start, tmpl.end)
        timeline.entityView = quranEntity
        activity.blurredImageView.addEntity(quranEntity)
    }

    private fun addEntityTrsl(tmpl: EntityTranslationTemplate) {
        val nameFont = tmpl.name_font ?: "ReadexPro_Medium.ttf"
        val translationQuranEntity = TranslationQuranEntity().apply {
            text = tmpl.aya ?: ""
            fontName = nameFont
            textColor = tmpl.color
            entityIndex = tmpl.number
            scale = tmpl.scale
            factorSize = tmpl.factor_size
            entityTransition = tmpl.transition ?: Transition()
        }
        val timeline = addTimeLineTrslQuran(translationQuranEntity, tmpl.start, tmpl.end)
        timeline.entityView = translationQuranEntity
        activity.blurredImageView.addEntity(translationQuranEntity)
    }

    private fun addEntityIsti3ada(tmpl: EntityBismilahTemplate) {
        val bismilahEntity = BismilahEntity().apply {
            text = tmpl.aya ?: "أعوذ بالله من الشيطان الرجيم"
            fontName = "خط الاستعاذه.ttf"
            textColor = tmpl.color
            scale = tmpl.factor_size
            entityTransition = tmpl.transition ?: Transition()
        }
        val timeline = addTimeLineIsti3ada(bismilahEntity, tmpl.start, tmpl.end)
        timeline.entityView = bismilahEntity
        activity.blurredImageView.setIsti3adhaEntity(bismilahEntity)
    }

    private fun addEntityBismilah(tmpl: EntityBismilahTemplate) {
        val bismilahEntity = BismilahEntity().apply {
            text = tmpl.aya ?: "بسم الله الرحمن الرحيم"
            fontName = "خط البسملة.ttf"
            textColor = tmpl.color
            scale = tmpl.factor_size
            entityTransition = tmpl.transition ?: Transition()
        }
        val timeline = addTimeLineBismilah(bismilahEntity, tmpl.start, tmpl.end)
        timeline.entityView = bismilahEntity
        activity.blurredImageView.setBismilahEntity(bismilahEntity)
    }

    fun checkSplitEntity() {
        // Check if split is possible for selected Quran entity
    }

    fun checkSplitTrslEntity() {
        // Check if split is possible for selected translation entity
    }

    fun checkSplitAudio() {
        // Check if split is possible for selected audio entity
    }

    fun addUpdateAnim(entityBismilahTimeline: EntityBismilahTimeline?, reference: EntityBismilahTimeline) {
        // Apply animation update from reference to all other bismilah entities
    }

    fun addUpdateAnim(entityQuranTimeline: EntityQuranTimeline?, reference: EntityQuranTimeline) {
        // Apply animation update from reference to all quran entities
    }
}
