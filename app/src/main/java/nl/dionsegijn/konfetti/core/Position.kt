package nl.dionsegijn.konfetti.core

sealed class Position {
    data class Absolute(val x: Float, val y: Float) : Position() {
        fun between(value: Absolute): Between = Between(this, value)
    }

    data class Relative(val x: Double, val y: Double) : Position() {
        fun between(value: Relative): Between = Between(this, value)
    }

    data class Between(val min: Position, val max: Position) : Position()
}
