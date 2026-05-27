package hazem.nurmontage.videoquran.model

class RenderManager {

    private val tasks = mutableListOf<RenderTask>()
    var currentTaskIndex: Int = 0
    var totalWeight: Float = 0f
        private set

    fun addTask(task: RenderTask) {
        tasks.add(task)
        totalWeight += task.weight
    }

    fun clear() {
        tasks.clear()
        currentTaskIndex = 0
        totalWeight = 0f
    }

    fun getProgress(): Float {
        if (tasks.isEmpty() || totalWeight == 0f) return 0f
        var completedWeight = 0f
        for (i in 0 until currentTaskIndex.coerceAtMost(tasks.size)) {
            completedWeight += tasks[i].weight
        }
        return (completedWeight / totalWeight).coerceIn(0f, 1f)
    }

    fun advanceToNext(): Boolean {
        if (currentTaskIndex < tasks.size - 1) {
            currentTaskIndex++
            return true
        }
        return false
    }

    fun isComplete(): Boolean = currentTaskIndex >= tasks.size

    fun getCurrentTaskName(): String =
        tasks.getOrNull(currentTaskIndex)?.name ?: ""
}
