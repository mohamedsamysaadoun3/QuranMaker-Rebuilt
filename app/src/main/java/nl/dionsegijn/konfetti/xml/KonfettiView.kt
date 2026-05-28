package nl.dionsegijn.konfetti.xml

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.View
import nl.dionsegijn.konfetti.core.Particle
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.PartySystem
import nl.dionsegijn.konfetti.core.models.CoreRectImpl
import nl.dionsegijn.konfetti.core.models.ReferenceImage
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.xml.image.DrawableImage
import nl.dionsegijn.konfetti.xml.image.ImageStore
import nl.dionsegijn.konfetti.xml.listeners.OnParticleSystemUpdateListener

class KonfettiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val systems: MutableList<PartySystem> = mutableListOf()
    private val imageStore: ImageStore = ImageStore()
    private val paint: Paint = Paint()
    private var drawArea: CoreRectImpl = CoreRectImpl(0f, 0f, 0f, 0f)
    private var timer: TimerIntegration = TimerIntegration()
    
    var onParticleSystemUpdateListener: OnParticleSystemUpdateListener? = null

    val activeSystems: List<PartySystem>
        get() = systems

    val isActive: Boolean
        get() = systems.isNotEmpty()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val deltaTime = timer.getDeltaTime()
        
        var size = systems.size
        while (--size >= 0) {
            val partySystem = systems[size]
            if (timer.getTotalTimeRunning(partySystem.createdAt) >= partySystem.party.delay) {
                partySystem.render(deltaTime, drawArea).forEach { display(it, canvas) }
            }
            if (partySystem.isDoneEmitting()) {
                systems.removeAt(size)
                onParticleSystemUpdateListener?.onParticleSystemEnded(this, partySystem.party, systems.size)
            }
        }
        
        if (systems.isNotEmpty()) {
            invalidate()
        } else {
            timer.reset()
        }
    }

    private fun display(particle: Particle, canvas: Canvas) {
        paint.color = particle.color
        val scaleX = (particle.scaleX * particle.width) / 2f
        val save = canvas.save()
        canvas.translate(particle.x - scaleX, particle.y)
        canvas.rotate(particle.rotation, scaleX, particle.width / 2f)
        canvas.scale(particle.scaleX, 1f)
        draw(particle.shape, canvas, paint, particle.width, imageStore)
        canvas.restoreToCount(save)
    }

    fun start(vararg party: Party) {
        val list = systems
        for (p in party) {
            onParticleSystemUpdateListener?.onParticleSystemStarted(this, p, systems.size)
            list.add(PartySystem(storeImages(p), pixelDensity = resources.displayMetrics.density))
        }
        invalidate()
    }

    fun start(party: List<Party>) {
        for (p in party) {
            onParticleSystemUpdateListener?.onParticleSystemStarted(this, p, systems.size)
            systems.add(PartySystem(storeImages(p), pixelDensity = resources.displayMetrics.density))
        }
        invalidate()
    }

    fun start(party: Party) {
        onParticleSystemUpdateListener?.onParticleSystemStarted(this, party, systems.size)
        systems.add(PartySystem(storeImages(party), pixelDensity = resources.displayMetrics.density))
        invalidate()
    }

    private fun storeImages(party: Party): Party {
        val newShapes = party.shapes.map { shape ->
            if (shape is Shape.DrawableShape) {
                shape.copy(image = drawableToReferenceImage(shape.image as DrawableImage))
            } else {
                shape
            }
        }
        return party.copy(shapes = newShapes)
    }

    fun drawableToReferenceImage(drawableImage: DrawableImage): ReferenceImage {
        return ReferenceImage(imageStore.storeImage(drawableImage.drawable), drawableImage.width, drawableImage.height)
    }

    fun stop(party: Party) {
        systems.removeAll { it.party == party }
        onParticleSystemUpdateListener?.onParticleSystemEnded(this, party, systems.size)
    }

    fun reset() {
        systems.clear()
    }

    fun stopGracefully() {
        systems.forEach { it.enabled = false }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        drawArea = CoreRectImpl(0f, 0f, w.toFloat(), h.toFloat())
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        timer.reset()
    }

    private class TimerIntegration {
        private var previousTime: Long = -1L

        fun reset() {
            previousTime = -1L
        }

        fun getDeltaTime(): Float {
            if (previousTime == -1L) {
                previousTime = System.nanoTime()
            }
            val delta = (System.nanoTime() - previousTime) / 1000000.0f
            previousTime = System.nanoTime()
            return delta / 1000f
        }

        fun getTotalTimeRunning(startTime: Long): Long {
            return System.currentTimeMillis() - startTime
        }
    }
}

private fun draw(shape: Shape, canvas: Canvas, paint: Paint, size: Float, imageStore: ImageStore) {
    when (shape) {
        Shape.Square -> canvas.drawRect(0f, 0f, size, size, paint)
        Shape.Circle -> {
            Shape.Circle.rect.set(0f, 0f, size, size)
            canvas.drawOval(
                RectF(Shape.Circle.rect.x, Shape.Circle.rect.y, Shape.Circle.rect.width, Shape.Circle.rect.height),
                paint
            )
        }
        is Shape.Rectangle -> {
            val heightRatio = shape.heightRatio * size
            val offset = (size - heightRatio) / 2f
            canvas.drawRect(0f, offset, size, offset + heightRatio, paint)
        }
        is Shape.DrawableShape -> {
            val image = (shape.image as? ReferenceImage)?.let { imageStore.getImage(it.reference) } ?: return
            if (shape.tint) {
                if (Build.VERSION.SDK_INT >= 29) {
                    image.setColorFilter(android.graphics.BlendModeColorFilter(paint.color, android.graphics.BlendMode.SRC_IN))
                } else {
                    @Suppress("DEPRECATION")
                    image.setColorFilter(paint.color, PorterDuff.Mode.SRC_IN)
                }
            } else if (shape.applyAlpha) {
                image.alpha = paint.alpha
            }
            val heightRatio = (shape.heightRatio * size).toInt()
            val offset = ((size - heightRatio) / 2f).toInt()
            image.setBounds(0, offset, size.toInt(), heightRatio + offset)
            image.draw(canvas)
        }
    }
}
