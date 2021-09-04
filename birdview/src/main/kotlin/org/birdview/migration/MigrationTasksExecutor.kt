package org.birdview.migration

import org.slf4j.LoggerFactory
import javax.inject.Named

@Named
class MigrationTasksExecutor(
    private val migrationTasks: List<MigrationTask>
) {
    private val log = LoggerFactory.getLogger(MigrationTasksExecutor::class.java)

    fun commenceMigration() {
      for (task in migrationTasks) {
          commenceTask(task)
      }
    }

    private fun commenceTask(task: MigrationTask): Unit = try {
        log.info("Running migration task: ${task.getName()}")
        task.commence()
    } catch (e: Exception) {
        log.error("Error execution migration task ${task.getName()}")
    }
}