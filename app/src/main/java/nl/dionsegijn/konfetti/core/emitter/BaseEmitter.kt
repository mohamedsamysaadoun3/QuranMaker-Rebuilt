package nl.dionsegijn.konfetti.core.emitter

import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.models.CoreRect

abstract class BaseEmitter {
    abstract fun createConfetti(deltaTime: Float, party: Party, drawArea: CoreRect): List<Confetti>
    abstract fun isFinished(): Boolean
}
