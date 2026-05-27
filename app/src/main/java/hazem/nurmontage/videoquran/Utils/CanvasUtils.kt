package hazem.nurmontage.videoquran.Utils

import android.graphics.Path
import android.graphics.RectF

/**
 * Custom rounded rect path drawing utility.
 */
object CanvasUtils {

    fun createRoundedRectPath(left: Float, top: Float, right: Float, bottom: Float, radius: Float): Path {
        val path = Path()
        val rect = RectF(left, top, right, bottom)
        path.addRoundRect(rect, radius, radius, Path.Direction.CW)
        return path
    }

    fun createRoundedRectPath(rect: RectF, radius: Float): Path {
        val path = Path()
        path.addRoundRect(rect, radius, radius, Path.Direction.CW)
        return path
    }
}
