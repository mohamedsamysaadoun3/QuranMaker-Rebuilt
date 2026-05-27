package nl.dionsegijn.konfetti.xml.image
import android.graphics.drawable.Drawable
object ImageUtil {
    fun loadDrawable(drawable: Drawable?, rotate: Boolean, scale: Boolean): Any = drawable ?: Unit
}
