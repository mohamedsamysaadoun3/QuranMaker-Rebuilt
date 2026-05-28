package nl.dionsegijn.konfetti.core.emitter

import nl.dionsegijn.konfetti.core.models.CoreRect
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.core.models.Vector
import kotlin.math.abs
import kotlin.math.max

class Confetti(
    var location: Vector,
    val color: Int,
    val width: Float,
    val mass: Float,
    val shape: Shape,
    var lifespan: Long = -1L,
    val fadeOut: Boolean = true,
    var acceleration: Vector = Vector(0f, 0f),
    var velocity: Vector = Vector(0f, 0f),
    var damping: Float = 1f,
    val rotationSpeed3D: Float = 1f,
    val rotationSpeed2D: Float = 1f,
    val pixelDensity: Float = 1f
) {
    companion object {
        private const val ALPHA_DECREMENT = 5
        private const val DEFAULT_FRAME_RATE = 60f
        private const val FULL_CIRCLE = 360f
        private const val GRAVITY = 0.02f
    }
    
    private var frameRate: Float = DEFAULT_FRAME_RATE
    private val gravity = Vector(0f, GRAVITY)
    var alpha: Int = 255
        private set
    var alphaColor: Int = 0
        private set
    var drawParticle: Boolean = true
        private set
    var rotation: Float = 0f
        private set
    private var rotationWidth: Float = width
    var scaleX: Float = 1f
        private set

    fun isDead(): Boolean = alpha <= 0

    fun applyForce(force: Vector) {
        acceleration.addScaled(force, 1f / mass)
    }

    fun render(deltaTime: Float, drawArea: CoreRect) {
        applyForce(gravity)
        update(deltaTime, drawArea)
    }

    private fun update(deltaTime: Float, drawArea: CoreRect) {
        frameRate = if (deltaTime > 0f) 1f / deltaTime else DEFAULT_FRAME_RATE
        if (location.y > drawArea.height) {
            alpha = 0
            return
        }
        velocity.add(acceleration)
        velocity.mult(damping)
        location.addScaled(velocity, frameRate * deltaTime * pixelDensity)
        lifespan -= (1000 * deltaTime).toLong()
        if (lifespan <= 0) {
            updateAlpha(deltaTime)
        }
        rotation += rotationSpeed2D * deltaTime * frameRate
        if (rotation >= FULL_CIRCLE) {
            rotation = 0f
        }
        rotationWidth -= abs(rotationSpeed3D) * deltaTime * frameRate
        if (rotationWidth < 0f) {
            rotationWidth = width
        }
        scaleX = abs(rotationWidth / width - 0.5f) * 2
        alphaColor = (alpha shl 24) or (color and 0x00FFFFFF)
        drawParticle = drawArea.contains(location.x.toInt(), location.y.toInt())
    }

    private fun updateAlpha(deltaTime: Float) {
        if (fadeOut) {
            alpha = max(0, alpha - (ALPHA_DECREMENT * deltaTime * frameRate).toInt())
        } else {
            alpha = 0
        }
    }
}
