package hazem.nurmontage.videoquran.common

import hazem.nurmontage.videoquran.model.MRectF

/**
 * Undo/redo stack entry for entity rect positions.
 */
data class StackEntity(
    val index: Int = 0,
    val rectF: MRectF = MRectF(),
    val action: String = ""
)
