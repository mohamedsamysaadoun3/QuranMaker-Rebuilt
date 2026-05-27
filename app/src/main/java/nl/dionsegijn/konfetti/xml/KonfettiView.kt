package nl.dionsegijn.konfetti.xml

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * Stub for the Konfetti confetti library (nl.dionsegijn:konfetti).
 *
 * The real implementation renders particle systems on a canvas.
 * This stub provides the three Android View constructors so that
 * DataBinding / layout inflation can succeed without the full library.
 *
 * Replace with the real library dependency when ready:
 *   implementation("nl.dionsegijn:konfetti:x.y.z")
 */
class KonfettiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint()

    /** Whether any particle system is currently active. */
    fun isActive(): Boolean = false

    /** Reset all particle systems. */
    fun reset() {
        // Stub
    }

    /** Start a confetti party – stub, does nothing. */
    fun start(vararg party: Any) {
        // Stub – original takes Party objects from the konfetti core library
    }

    /** Stop gracefully. */
    fun stopGracefully() {
        // Stub
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Stub – particle rendering would go here
    }
}
