package nl.dionsegijn.konfetti.core

import nl.dionsegijn.konfetti.core.emitter.EmitterConfig
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.core.models.Size

data class Party(
    val angle: Int = 0,
    val spread: Int = 360,
    val speed: Float = 30f,
    val maxSpeed: Float = 0f,
    val damping: Float = 0.9f,
    val size: List<Size> = listOf(Size.SMALL, Size.MEDIUM, Size.LARGE),
    val colors: List<Int> = listOf(16572810, 16740973, 16003181, 11832815),
    val shapes: List<Shape> = listOf(Shape.Square, Shape.Circle),
    val timeToLive: Long = 10000L,
    val fadeOutEnabled: Boolean = true,
    val position: Position = Position.Relative(0.5, 0.5),
    val delay: Int = 0,
    val rotation: Rotation = Rotation(),
    val emitter: EmitterConfig
)
