package org.birdview

import org.birdview.api.BVTaskService
import org.birdview.command.TaskListCommand
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import picocli.CommandLine

fun main(vararg args:String) {
    AnnotationConfigApplicationContext(BirdviewConfiguration::class.java).use {ctx->
        val taskService =  ctx.getBean(BVTaskService::class.java)
        val groupDescriber = ctx.getBean(GroupDescriber::class.java)
        CommandLine(TaskListCommand(taskService))
                .also { it.isCaseInsensitiveEnumValuesAllowed = true }
                .execute(*args)
    }
}
