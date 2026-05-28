package nl.dionsegijn.konfetti.core.emitter

import java.util.concurrent.TimeUnit

data class Emitter(val duration: Long, val timeUnit: TimeUnit = TimeUnit.MILLISECONDS) {
    fun max(amount: Int): EmitterConfig = EmitterConfig(this).max(amount)
    fun perSecond(amount: Int): EmitterConfig = EmitterConfig(this).perSecond(amount)
}
