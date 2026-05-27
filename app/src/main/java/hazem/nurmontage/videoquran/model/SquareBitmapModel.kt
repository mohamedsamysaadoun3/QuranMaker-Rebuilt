package hazem.nurmontage.videoquran.model

import java.io.Serializable

class SquareBitmapModel(
    var lef_square: Float = 50.0f,
    var top_square: Float = 0f,
    var right: Float = 0f,
    var bottom: Float = 0f,
    var width_sqaure: Float = 50.0f,
    var height_square: Float = 50.0f,
    var raduis: Float = 0f
) : Serializable {

    var posX: Float = 0f
    var posY: Float = 0f

    constructor() : this(50.0f, 0f, 0f, 0f, 50.0f, 50.0f, 0f)
}
