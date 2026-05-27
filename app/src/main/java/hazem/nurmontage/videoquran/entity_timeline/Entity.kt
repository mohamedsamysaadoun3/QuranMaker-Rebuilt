package hazem.nurmontage.videoquran.entity_timeline

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

    enum class EntityType { QURAN, TRANSLATION, BISMILAH, AUDIO, TEXT, SURAH_NAME }
}
