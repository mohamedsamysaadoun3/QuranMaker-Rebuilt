package nl.dionsegijn.konfetti.xml.image

import android.graphics.drawable.Drawable
import nl.dionsegijn.konfetti.core.models.Shape

object ImageUtil {
    @JvmStatic
    @JvmOverloads
    fun loadDrawable(drawable: Drawable?, tint: Boolean = true, applyAlpha: Boolean = true): Shape.DrawableShape {
        requireNotNull(drawable) { "drawable must not be null" }
        return Shape.DrawableShape(DrawableImage(drawable, drawable.intrinsicWidth, drawable.intrinsicHeight), tint, applyAlpha)
    }
}
