package nl.dionsegijn.konfetti.core

open class Position

class Relative(val x: Double, val y: Double) : Position()
class Absolute(val x: Int, val y: Int) : Position()
