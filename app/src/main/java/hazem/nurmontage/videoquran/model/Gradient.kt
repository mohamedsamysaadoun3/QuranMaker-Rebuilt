package hazem.nurmontage.videoquran.model

import java.io.Serializable

/**
 * Represents a 3-color gradient with an angle.
 * Used for background gradients in the video editor.
 */
data class Gradient(
    val color: Int,
    val second: Int,
    val three: Int,
    var angle: Int = 81
) : Serializable
