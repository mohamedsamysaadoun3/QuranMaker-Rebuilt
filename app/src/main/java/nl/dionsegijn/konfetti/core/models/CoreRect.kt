package nl.dionsegijn.konfetti.core.models

interface CoreRect {
    var x: Float
    var y: Float
    var width: Float
    var height: Float
    
    fun set(x: Float, y: Float, width: Float, height: Float) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
    }
    
    fun contains(px: Int, py: Int): Boolean {
        val f = px.toFloat()
        val f2 = py.toFloat()
        return f >= x && f <= x + width && f2 >= y && f2 <= y + height
    }
}
