package hazem.nurmontage.videoquran.model

import java.io.Serializable

class TimeModel(
    var width_bitmap_progress: Int = 0,
    var height_bitmap_progress: Int = 0,
    var size: Float = 0f,
    var color: String? = null,
    var posY: Float = 0f,
    var posXRight: Float = 0f,
    var progress_offset: Int = 0
) : Serializable {

    var heightShape: Int = 0
    var widthShape: Int = 0
    var startShape: Float = 0f
}
