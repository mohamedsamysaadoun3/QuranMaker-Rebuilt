package nl.dionsegijn.konfetti.core.models

data class CoreRectImpl(
    override var x: Float = 0f,
    override var y: Float = 0f,
    override var width: Float = 0f,
    override var height: Float = 0f
) : CoreRect
