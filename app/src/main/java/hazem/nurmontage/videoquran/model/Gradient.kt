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
) : Serializable

// Backward compatibility - keep old constructor pattern
// Common.kt uses Gradient(-711565, -6000461, -10897425) which maps to startColor, centerColor, endColor
