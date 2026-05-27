package hazem.nurmontage.videoquran.model

import java.io.Serializable

class Transition(
    var type_in: String = "none",
    var type_out: String = "none",
    var type_both: String = "none",
    var duration_in: Float = 1.5f,
    var duration_out: Float = 1.5f,
    var duration_both: Float = 0.1f,
    var isIn: Boolean = false,
    var isOut: Boolean = false,
    var isBoth: Boolean = false
) : Serializable {

    var offset_frame_in: Float = 0f
    var offset_frame_out: Float = 0f
    var fromW: Float = 0f

    fun duplicate(): Transition = Transition(
        type_in, type_out, type_both,
        duration_in, duration_out, duration_both,
        isIn, isOut, isBoth
    ).also {
        it.offset_frame_in = offset_frame_in
        it.offset_frame_out = offset_frame_out
        it.fromW = fromW
    }
}
