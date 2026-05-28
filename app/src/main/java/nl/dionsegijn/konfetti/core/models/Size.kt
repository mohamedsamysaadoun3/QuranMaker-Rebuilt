package nl.dionsegijn.konfetti.core.models

data class Size(val sizeInDp: Int, val mass: Float = 5f, val massVariance: Float = 0.2f) {
    companion object {
        val SMALL = Size(6, 4f, 0f)
        val MEDIUM = Size(8, 0.01f, 0f)
        val LARGE = Size(10, 6f, 0f)
    }
}
