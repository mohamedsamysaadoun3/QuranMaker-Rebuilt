package nl.dionsegijn.konfetti.core.emitter

import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Rotation
import nl.dionsegijn.konfetti.core.models.CoreRect
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.core.models.Size
import nl.dionsegijn.konfetti.core.models.Vector
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class PartyEmitter(
    private val emitterConfig: EmitterConfig,
    private val pixelDensity: Float,
    private val random: Random = Random.Default
) : BaseEmitter() {
    
    private var createParticleMs: Float = 0f
    private var elapsedTime: Float = 0f
    private var particlesCreated: Int = 0

    override fun createConfetti(deltaTime: Float, party: Party, drawArea: CoreRect): List<Confetti> {
        createParticleMs += deltaTime
        val emittingTime = emitterConfig.emittingTime / 1000f
        if (elapsedTime == 0f && deltaTime > emittingTime) {
            createParticleMs = emittingTime
        }
        var result: List<Confetti> = emptyList()
        if (createParticleMs >= emitterConfig.amountPerMs && !isTimeElapsed()) {
            val count = (createParticleMs / emitterConfig.amountPerMs).toInt()
            result = (1..count).map { createParticle(party, drawArea) }
            createParticleMs %= emitterConfig.amountPerMs
        }
        elapsedTime += deltaTime * 1000
        return result
    }

    private fun createParticle(party: Party, drawArea: CoreRect): Confetti {
        particlesCreated++
        val size = party.size[random.nextInt(party.size.size)]
        val absolute = getPosition(party.position, drawArea)
        return Confetti(
            location = Vector(absolute.x, absolute.y),
            color = party.colors[random.nextInt(party.colors.size)],
            width = size.sizeInDp * pixelDensity,
            mass = massWithVariance(size),
            shape = getRandomShape(party.shapes),
            lifespan = party.timeToLive,
            fadeOut = party.fadeOutEnabled,
            velocity = getVelocity(party),
            damping = party.damping,
            rotationSpeed3D = rotationSpeed(party.rotation) * party.rotation.multiplier3D,
            rotationSpeed2D = rotationSpeed(party.rotation) * party.rotation.multiplier2D,
            pixelDensity = pixelDensity
        )
    }

    private fun rotationSpeed(rotation: Rotation): Float {
        if (!rotation.enabled) return 0f
        return rotation.speed + rotation.speed * rotation.variance * (random.nextFloat() * 2f - 1f)
    }

    private fun getSpeed(party: Party): Float {
        if (party.maxSpeed == -1f) return party.speed
        return party.speed + (party.maxSpeed - party.speed) * random.nextFloat()
    }

    private fun massWithVariance(size: Size): Float {
        return size.mass + size.mass * random.nextFloat() * size.massVariance
    }

    private fun getVelocity(party: Party): Vector {
        val speed = getSpeed(party)
        val radians = Math.toRadians(getAngle(party))
        return Vector((cos(radians) * speed).toFloat(), (sin(radians) * speed).toFloat())
    }

    private fun getAngle(party: Party): Double {
        if (party.spread == 0) return party.angle.toDouble()
        val minAngle = party.angle - party.spread / 2.0
        val maxAngle = party.angle + party.spread / 2.0
        return minAngle + random.nextDouble() * (maxAngle - minAngle)
    }

    private fun getPosition(position: Position, coreRect: CoreRect): Position.Absolute {
        return when (position) {
            is Position.Absolute -> Position.Absolute(position.x, position.y)
            is Position.Relative -> Position.Absolute(
                coreRect.width * position.x.toFloat(),
                coreRect.height * position.y.toFloat()
            )
            is Position.Between -> {
                val min = getPosition(position.min, coreRect)
                val max = getPosition(position.max, coreRect)
                Position.Absolute(
                    random.nextFloat() * (max.x - min.x) + min.x,
                    random.nextFloat() * (max.y - min.y) + min.y
                )
            }
        }
    }

    private fun getRandomShape(shapes: List<Shape>): Shape {
        return shapes[random.nextInt(shapes.size)]
    }

    private fun isTimeElapsed(): Boolean {
        return emitterConfig.emittingTime != 0L && elapsedTime >= emitterConfig.emittingTime.toFloat()
    }

    override fun isFinished(): Boolean {
        return emitterConfig.emittingTime > 0 && elapsedTime >= emitterConfig.emittingTime.toFloat()
    }
}
