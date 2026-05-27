package nl.dionsegijn.konfetti.core.emitter
import java.util.concurrent.TimeUnit
class Emitter(val duration: Long, val unit: TimeUnit) {
    private var maxCount: Int = 0
    fun max(count: Int): Emitter { maxCount = count; return this }
}
