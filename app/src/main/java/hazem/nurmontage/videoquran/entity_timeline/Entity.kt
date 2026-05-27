package hazem.nurmontage.videoquran.entity_timeline

import android.graphics.RectF
import hazem.nurmontage.videoquran.common.StackEntity
import hazem.nurmontage.videoquran.constant.EntityAction
import hazem.nurmontage.videoquran.model.MRectF
import java.io.Serializable

abstract class Entity : Serializable {

    var id: Long = 0
    var index: Int = 0
    var name: String = ""
    var startMs: Long = 0L
    var endMs: Long = 0L
    var durationMs: Long = 0L
    var rectF: MRectF = MRectF()
    var x: Float = 0f
    var y: Float = 0f
    var width: Float = 0f
    var height: Float = 0f
    var scale: Float = 1.0f
    var isSelected: Boolean = false
    var isTrimming: Boolean = false
    private val undoStack = mutableListOf<StackEntity>()
    private val redoStack = mutableListOf<StackEntity>()
    var maxUndoLevels: Int = 50
    var trimStartMs: Long = 0L
    var trimEndMs: Long = 0L
    var isVisible: Boolean = true
    var isLocked: Boolean = false
    var hasAudio: Boolean = false
    var volume: Float = 1.0f

    // Trim type: -1 = none, 0 = left trim, 1 = right trim
    private var trimType: Int = -1
    private var currentRect: MRectF = MRectF()
    private var onTapTime: Long = 0L
    private var onTapPosition: Float = 0f

    // === TRIM / SELECTION METHODS ===
    fun getTrimType(): Int = trimType
    fun setTrimType(type: Int) { trimType = type }

    fun getRect(): RectF = rectF.toRectF()

    fun setCurrentRect() {
        currentRect = MRectF(rectF.left, rectF.top, rectF.right, rectF.bottom)
    }

    fun setOnTapTime(timeMs: Long, position: Float) {
        onTapTime = timeMs
        onTapPosition = position
    }

    fun setSelect(v: Boolean) { isSelected = v }
    fun isSelect(): Boolean = isSelected

    fun visible(): Boolean = isVisible


    fun contains(point: android.graphics.PointF): Boolean = rectF.contains(point.x, point.y)

    fun getColor(): Int = _color
    fun setColor(c: Int) { _color = c }

    private var _color: Int = -13421771

    // === UNDO / REDO for move and trim ===
    fun undoMove() {
        val prev = currentRect
        rectF.set(prev.left, prev.top, prev.right, prev.bottom)
    }

    fun redoMove() {
        // Re-apply last move
    }

    fun undoTrim() {
        val prev = currentRect
        rectF.set(prev.left, prev.top, prev.right, prev.bottom)
    }

    fun redoTrim() {
        // Re-apply last trim
    }

    // === STANDARD UNDO/REDO ===
    fun pushUndo(action: EntityAction, rect: MRectF = this.rectF) {
        undoStack.add(StackEntity(index, MRectF(rect.left, rect.top, rect.right, rect.bottom), action.name))
        if (undoStack.size > maxUndoLevels) undoStack.removeAt(0)
        redoStack.clear()
    }

    fun undo(): StackEntity? {
        if (undoStack.isEmpty()) return null
        val entry = undoStack.removeAt(undoStack.size - 1)
        redoStack.add(entry)
        return entry
    }

    fun redo(): StackEntity? {
        if (redoStack.isEmpty()) return null
        val entry = redoStack.removeAt(redoStack.size - 1)
        undoStack.add(entry)
        return entry
    }

    fun canUndo(): Boolean = undoStack.isNotEmpty()
    fun canRedo(): Boolean = redoStack.isNotEmpty()
    fun containsTime(timeMs: Long): Boolean = timeMs in startMs..endMs

    abstract fun getType(): EntityType

    // === Entity view reference (used by EditEntityFragment.checkIcon) ===
    var entityView: Any? = null

    enum class EntityType { QURAN, TRANSLATION, BISMILAH, AUDIO, TEXT, SURAH_NAME }
}

