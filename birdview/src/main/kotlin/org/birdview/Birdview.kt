package org.birdview

import org.birdview.command.TaskListCommand
import org.birdview.web.ReportWebService
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import picocli.CommandLine

fun main(vararg args:String) {
    AnnotationConfigApplicationContext(BirdviewConfiguration::class.java).use {ctx->
        val taskService =  ctx.getBean(BVTaskService::class.java)
        val reportWebService =  ctx.getBean(ReportWebService::class.java)
        val groupDescriber = ctx.getBean(GroupDescriber::class.java)
        CommandLine(TaskListCommand(taskService, reportWebService))
                .also { it.isCaseInsensitiveEnumValuesAllowed = true }
                .execute(*args)
    }
}
