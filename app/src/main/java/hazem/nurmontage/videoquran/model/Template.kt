package hazem.nurmontage.videoquran.model

import hazem.nurmontage.videoquran.Utils.MFileUtils
import hazem.nurmontage.videoquran.constant.IpadType
import hazem.nurmontage.videoquran.constant.ResizeType
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class Template : Serializable {

    var currentCursur: Int = 0
    var duration: Int = 0
    var duration_video_media: Int = 0
    var entityBismilahTemplate: EntityBismilahTemplate? = null
    var entityIsti3adaTemplate: EntityBismilahTemplate? = null
    var entityProgressTemplate: EntityProgressTemplate? = null
    var entitySurahTemplate: EntitySurahTemplate? = null
    var extension: String? = null
    var fileInfo: MFileUtils.FileInfo? = null
    var folder_template: String? = null
    var frame_bg: String? = null
    var gradient: Gradient? = null
    var height: Int = 0
    var idTemplate: String? = null
    var isGlass: Boolean = false
    var isNewCode: Boolean = false
    var isVideoSquare: Boolean = false
    var mDrawingTranslationX: Float = 0f
    var mDrawingTranslationY: Float = 0f
    var mTimeModel: TimeModel? = null
    var name_drawable: String? = null
    var squareBitmapModel: SquareBitmapModel? = null
        get() {
            if (field == null) {
                field = SquareBitmapModel()
            }
            return field
        }
    var uri_bg: String? = null
    var uri_bg_ffmpeg: String? = null
    var uri_media_video: String? = null
    var uri_original_upload_video: String? = null
    var uri_upload_extract_audio_video: String? = null
    var uri_video: String? = null
    var width: Int = 0
    var resolution: String = "720p"
    var scale_timeline: Float = 0.5f
    var fps: Int = 30
    var resizeType: Int = ResizeType.SOCIAL_STORY.ordinal
    var imgResize: String = "i_9:16"
    var x_square: Float = 0.3f
    var y_square: Float = 0.2f
    var width_square: Float = 0.37218544f
    var height_square: Float = 0.41986755f
    var color_ipad: Int = -1
    var ipad_type: Int = IpadType.IPAD.ordinal
    var index_color: Int = -1

    val entityMediaList: MutableList<EntityMedia> = mutableListOf()
    val quranEntityList: MutableList<EntityQuranTemplate> = mutableListOf()
    val translationTemplateList: MutableList<EntityTranslationTemplate> = mutableListOf()

    fun addMedia(entityMedia: EntityMedia) {
        entityMediaList.add(entityMedia)
    }

    fun addQuranEntityList(entityQuranTemplate: EntityQuranTemplate) {
        quranEntityList.add(entityQuranTemplate)
    }

    fun addTrslEntityList(entityTranslationTemplate: EntityTranslationTemplate) {
        translationTemplateList.add(entityTranslationTemplate)
    }

    fun setDrawingTranslation(x: Float, y: Float) {
        mDrawingTranslationX = x
        mDrawingTranslationY = y
    }

    fun setWidthAndHeight(w: Int, h: Int) {
        width = w
        height = h
    }

    fun duplicate(): Template? {
        return try {
            val baos = ByteArrayOutputStream()
            val oos = ObjectOutputStream(baos)
            oos.writeObject(this)
            oos.flush()
            val ois = ObjectInputStream(ByteArrayInputStream(baos.toByteArray()))
            ois.readObject() as Template
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun geTypeResize(): Int = resizeType
}
