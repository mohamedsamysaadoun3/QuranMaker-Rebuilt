package nl.dionsegijn.konfetti.core

data class Rotation(
    val enabled: Boolean = false,
    val speed: Float = 1f,
    val variance: Float = 0.5f,
    val multiplier2D: Float = 8f,
    val multiplier3D: Float = 1.5f
) {
    companion object {
        fun enabled() = Rotation(enabled = true)
        fun disabled() = Rotation(enabled = false)
    }
}
