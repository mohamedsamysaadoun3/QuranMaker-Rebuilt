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

    /** Convenience method matching original Java: addTask(name, weight) */
    fun addTask(name: String, weight: Int) {
        addTask(RenderTask(name = name, weight = weight.toFloat()))
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

    /** Alias matching original Java: nextTask() */
    fun nextTask() {
        advanceToNext()
    }

    fun isComplete(): Boolean = currentTaskIndex >= tasks.size

    fun getCurrentTaskName(): String =
        tasks.getOrNull(currentTaskIndex)?.name ?: ""
}
