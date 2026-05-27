package hazem.nurmontage.videoquran.model

import java.io.Serializable

class EntityBismilahTemplate(
    var transition: Transition? = null,
    var start: Float = 0f,
    var end: Float = 0f,
    var btm_x: Float = 0f,
    var btm_y: Float = 0f,
    var left: Float = 0f,
    var right: Float = 0f,
    var aya: String? = null,
    var color: Int = 0,
    var preset: Int = 0
) : Serializable {

    var file: String? = null
    var file_in: String? = null
    var file_out: String? = null
    var height: Float = 0f
    var x: Float = 0f
    var y: Float = 0f
    var scale: Float = 1.0f
    var factor_size: Float = 1.0f
    var rectF: MRectF? = null
}
