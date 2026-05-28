package nl.dionsegijn.konfetti.core.models

interface Shape {
    object Square : Shape
    object Circle : Shape {
        val rect: CoreRectImpl = CoreRectImpl(0f, 0f, 0f, 0f)
    }
    data class Rectangle(val heightRatio: Float) : Shape {
        init {
            require(heightRatio in 0f..1f)
        }
    }
    data class DrawableShape(val image: CoreImage, val tint: Boolean = true, val applyAlpha: Boolean = true) : Shape {
        val heightRatio: Float = when {
            image.height == -1 && image.width == -1 -> 1f
            image.height == -1 || image.width == -1 -> 0f
            else -> image.height.toFloat() / image.width.toFloat()
        }
    }
}
