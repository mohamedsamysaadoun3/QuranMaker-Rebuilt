package nl.dionsegijn.konfetti.core.emitter

import java.util.concurrent.TimeUnit

class EmitterConfig(emitter: Emitter) {
    var emittingTime: Long = TimeUnit.MILLISECONDS.convert(emitter.duration, emitter.timeUnit)
        private set
    var amountPerMs: Float = 0f
        private set

    fun max(amount: Int): EmitterConfig {
        amountPerMs = (emittingTime / amount.toFloat()) / 1000f
        return this
    }

    fun perSecond(amount: Int): EmitterConfig {
        amountPerMs = 1f / amount.toFloat()
        return this
    }
}
