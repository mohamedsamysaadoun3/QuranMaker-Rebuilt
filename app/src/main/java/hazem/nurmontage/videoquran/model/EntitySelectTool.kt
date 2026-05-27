package hazem.nurmontage.videoquran.model

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import java.io.Serializable

/**
 * Selection tool for entities on canvas.
 * Handles scale/move/apply-all UI handles and interaction logic.
 */
class EntitySelectTool : Serializable {

    private var canvasWidth: Int = 0
    private var context: Context? = null

    /** Whether a click-apply action is in progress */
    private var clickApply: Boolean = false

    /** Whether we are in move mode */
    private var applyMove: Boolean = false

    /** Whether we are in scale mode */
    private var applyScale: Boolean = false

    /** Whether apply-all is active */
    private var applyAll: Boolean = false

    /** Whether scale is in progress (finger is down) */
    private var onProgress: Boolean = false

    /** Scale handle position */
    var scaleHandleX: Float = 0f
    var scaleHandleY: Float = 0f

    /** Move handle position */
    var moveHandleX: Float = 0f
    var moveHandleY: Float = 0f

    /** Apply-all handle position */
    var applyAllHandleX: Float = 0f
    var applyAllHandleY: Float = 0f

    /** Handle size in pixels */
    var handleSize: Float = 24f

    /** Touch slop for handle detection */
    var touchSlop: Float = 16f

    /** Paints for drawing handles */
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val handleStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val selectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
        pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    constructor()

    constructor(canvasWidth: Int, context: Context) {
        this.canvasWidth = canvasWidth
        this.context = context
    }

    // === Handle detection ===

    fun isApply(entity: EntityView, x: Float, y: Float): Boolean {
        updateHandlePositions(entity)
        return isTouchingScaleHandle(x, y) || isTouchingMoveHandle(x, y) || isTouchingApplyAllHandle(x, y)
    }

    fun isApply_Move(): Boolean = applyMove

    fun isApply_Scale(): Boolean = applyScale

    fun isApply_all(): Boolean = applyAll

    fun setApply_all(v: Boolean) {
        applyAll = v
    }

    fun isClick_apply(): Boolean = clickApply

    fun setClick_apply(v: Boolean) {
        clickApply = v
    }

    fun isOnProgress(): Boolean = onProgress

    fun setOnProgress(v: Boolean) {
        onProgress = v
    }

    fun isScale(entity: EntityView, x: Float, y: Float) {
        updateHandlePositions(entity)
        when {
            isTouchingScaleHandle(x, y) -> {
                applyScale = true
                applyMove = false
            }
            isTouchingMoveHandle(x, y) -> {
                applyMove = true
                applyScale = false
            }
            isTouchingApplyAllHandle(x, y) -> {
                applyAll = true
            }
            else -> {
                applyMove = true
                applyScale = false
            }
        }
    }

    fun isTouchingScaleHandle(x: Float, y: Float): Boolean {
        val dx = x - scaleHandleX
        val dy = y - scaleHandleY
        return (dx * dx + dy * dy) <= (handleSize + touchSlop) * (handleSize + touchSlop)
    }

    fun isTouchingMoveHandle(x: Float, y: Float): Boolean {
        val dx = x - moveHandleX
        val dy = y - moveHandleY
        return (dx * dx + dy * dy) <= (handleSize + touchSlop) * (handleSize + touchSlop)
    }

    fun isTouchingApplyAllHandle(x: Float, y: Float): Boolean {
        val dx = x - applyAllHandleX
        val dy = y - applyAllHandleY
        return (dx * dx + dy * dy) <= (handleSize + touchSlop) * (handleSize + touchSlop)
    }

    // === Reset ===

    fun reset() {
        applyMove = false
        applyScale = false
        applyAll = false
        clickApply = false
        onProgress = false
    }

    fun resetHandles() {
        reset()
    }

    // === Update handle positions ===

    private fun updateHandlePositions(entity: EntityView) {
        val rect = entity.getRect()
        // Scale handle at bottom-right corner
        scaleHandleX = rect.right
        scaleHandleY = rect.bottom
        // Move handle at top-center
        moveHandleX = rect.centerX()
        moveHandleY = rect.top
        // Apply-all handle at top-right
        applyAllHandleX = rect.right
        applyAllHandleY = rect.top
    }

    // === Draw selection UI ===

    fun draw(canvas: Canvas, entity: EntityView) {
        updateHandlePositions(entity)
        val rect = entity.getRect()

        // Draw dashed selection rectangle
        canvas.drawRect(rect, selectionPaint)

        // Draw scale handle (bottom-right)
        canvas.drawCircle(scaleHandleX, scaleHandleY, handleSize, handlePaint)
        canvas.drawCircle(scaleHandleX, scaleHandleY, handleSize, handleStrokePaint)

        // Draw move handle (top-center)
        canvas.drawCircle(moveHandleX, moveHandleY, handleSize, handlePaint)
        canvas.drawCircle(moveHandleX, moveHandleY, handleSize, handleStrokePaint)

        // Draw apply-all handle (top-right)
        canvas.drawCircle(applyAllHandleX, applyAllHandleY, handleSize, handlePaint)
        canvas.drawCircle(applyAllHandleX, applyAllHandleY, handleSize, handleStrokePaint)
    }

    var isSelected: Boolean = false
    var isScaleHandle: Boolean = false
    var isMoveHandle: Boolean = false
    var isApplyAllHandle: Boolean = false
}
