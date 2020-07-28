package org.birdview

import org.birdview.command.BirdviewCommand
import org.birdview.command.TaskListCommand
import org.birdview.command.WebServerCommand
import org.birdview.web.ReportWebService
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import picocli.CommandLine

fun main(vararg args:String) {
    AnnotationConfigApplicationContext(BirdviewConfiguration::class.java).use {ctx->
        val taskService =  ctx.getBean(BVTaskService::class.java)
        val reportWebService = ctx.getBean(ReportWebService::class.java)
        CommandLine(BirdviewCommand())
                .addSubcommand(TaskListCommand(taskService))
                .addSubcommand(WebServerCommand(reportWebService))
                .also { it.isCaseInsensitiveEnumValuesAllowed = true }
                .execute(*args)
    }
}
