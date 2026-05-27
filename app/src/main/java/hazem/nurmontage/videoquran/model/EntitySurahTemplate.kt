package hazem.nurmontage.videoquran.model

import java.io.Serializable

class EntitySurahTemplate(
    var name: String? = null,
    var reader: String? = null,
    var left: Float = 0f,
    var top: Float = 0f,
    var rectF: MRectF? = null,
    var factor_scale: Float = 0f,
    var name_font: String? = null,
    var clr: Int = 0,
    var preset: Int = 0,
    var style: Int = 0,
    var index_surah: Int = 0,
    var isHaveBg: Boolean = false,
    var clrBg: Int = 0
) : Serializable
