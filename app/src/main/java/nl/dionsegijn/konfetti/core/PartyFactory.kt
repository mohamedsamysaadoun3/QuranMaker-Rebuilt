package nl.dionsegijn.konfetti.core
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.Absolute
class PartyFactory(private val emitter: Emitter) {
    private var spread: Spread = Spread.WIDE
    private var shapes: List<Any> = listOf()
    private var colors: List<Int> = listOf()
    private var speedMin: Float = 0f
    private var speedMax: Float = 10f
    private var position: Position = Absolute(0, 0)
    fun spread(spread: Spread): PartyFactory { this.spread = spread; return this }
    fun shapes(shapes: List<Any>): PartyFactory { this.shapes = shapes; return this }
    fun colors(colors: List<Int>): PartyFactory { this.colors = colors; return this }
    fun setSpeedBetween(min: Float, max: Float): PartyFactory { speedMin = min; speedMax = max; return this }
    fun position(position: Position): PartyFactory { this.position = position; return this }
    fun getParty(): Any = Unit
}
