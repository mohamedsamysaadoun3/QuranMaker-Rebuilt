package nl.dionsegijn.konfetti.core

import nl.dionsegijn.konfetti.core.emitter.BaseEmitter
import nl.dionsegijn.konfetti.core.emitter.Confetti
import nl.dionsegijn.konfetti.core.emitter.PartyEmitter
import nl.dionsegijn.konfetti.core.models.CoreRect

class PartySystem(
    val party: Party,
    val createdAt: Long = System.currentTimeMillis(),
    pixelDensity: Float
) {
    private val activeParticles: MutableList<Confetti> = mutableListOf()
    var enabled: Boolean = true
    private var emitter: BaseEmitter = PartyEmitter(party.emitter, pixelDensity)

    fun render(deltaTime: Float, drawArea: CoreRect): List<Particle> {
        if (enabled) {
            activeParticles.addAll(emitter.createConfetti(deltaTime, party, drawArea))
        }
        activeParticles.forEach { it.render(deltaTime, drawArea) }
        activeParticles.removeAll { it.isDead() }
        return activeParticles.filter { it.drawParticle }.map { confetti ->
            Particle(
                x = confetti.location.x,
                y = confetti.location.y,
                width = confetti.width,
                height = confetti.width,
                color = confetti.alphaColor,
                rotation = confetti.rotation,
                scaleX = confetti.scaleX,
                shape = confetti.shape,
                alpha = confetti.alpha
            )
        }
    }

    fun isDoneEmitting(): Boolean {
        return (emitter.isFinished() && activeParticles.isEmpty()) ||
                (!enabled && activeParticles.isEmpty())
    }

    fun getActiveParticleAmount(): Int = activeParticles.size
}
