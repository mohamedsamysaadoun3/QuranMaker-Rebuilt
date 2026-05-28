package nl.dionsegijn.konfetti.xml.image

import android.graphics.drawable.Drawable
import nl.dionsegijn.konfetti.core.models.CoreImageStore

class ImageStore : CoreImageStore<Drawable> {
    private val images: MutableMap<Int, Drawable> = mutableMapOf()

    override fun storeImage(image: Drawable): Int {
        val hashCode = image.hashCode()
        images[hashCode] = image
        return hashCode
    }

    override fun getImage(id: Int): Drawable? = images[id]
}
