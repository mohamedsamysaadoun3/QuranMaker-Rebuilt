package hazem.nurmontage.videoquran.model

import java.io.Serializable

/**
 * Selection tool for entities on canvas.
 * Handles scale/move/apply-all UI handles.
 */
class EntitySelectTool : Serializable {

    var isSelected: Boolean = false
    var isScaleHandle: Boolean = false
    var isMoveHandle: Boolean = false
    var isApplyAllHandle: Boolean = false

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

    fun resetHandles() {
        isSelected = false
        isScaleHandle = false
        isMoveHandle = false
        isApplyAllHandle = false
    }
}
