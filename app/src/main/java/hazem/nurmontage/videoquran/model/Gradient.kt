package hazem.nurmontage.videoquran.model

import java.io.Serializable

/**
 * Represents a 3-color gradient with an angle.
 * Used for background gradients in the video editor.
 */
data class Gradient(
    val startColor: Int,
    val centerColor: Int,
    val endColor: Int,
    var angle: Int = 81
) : Serializable {

    /** Java compatibility aliases used by GradientAdabter */
    fun getColor(): Int = startColor
    fun getSecond(): Int = centerColor
    fun getThree(): Int = endColor
}
