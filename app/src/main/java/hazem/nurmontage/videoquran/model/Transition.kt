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

    companion object {
        // Transition type constants used by BlurredImageView
        @JvmField
        val SLIDE_IN_LEFT = Transition(type_in = "slide_in_left", isIn = true)

        @JvmField
        val SLIDE_IN_RIGHT = Transition(type_in = "slide_in_right", isIn = true)

        @JvmField
        val SLIDE_OUT_LEFT = Transition(type_out = "slide_out_left", isOut = true)

        @JvmField
        val SLIDE_OUT_RIGHT = Transition(type_out = "slide_out_right", isOut = true)

        @JvmField
        val FADE_IN = Transition(type_in = "fade_in", isIn = true)

        @JvmField
        val FADE_OUT = Transition(type_out = "fade_out", isOut = true)

        @JvmField
        val NONE = Transition()
    }
}
