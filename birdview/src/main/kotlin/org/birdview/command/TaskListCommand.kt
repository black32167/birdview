package org.birdview.command

import org.birdview.analysis.BVDocument
import org.birdview.api.BVTaskService
import org.birdview.request.TasksRequest
import org.birdview.utils.BVColorUtils
import org.birdview.utils.BVColorUtils.bold
import picocli.CommandLine
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Callable

@CommandLine.Command(name = "list", mixinStandardHelpOptions = true,
        description = ["Lists tasks."])
class TaskListCommand(val taskService: BVTaskService) : Callable<Int> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            .also { it.timeZone = TimeZone.getTimeZone("UTC") }

    @CommandLine.Option(names = ["-s", "--status"], description = ["any|progress|todo|done"])
    var status = "progress"

    @CommandLine.Option(names = ["-t", "--type"], description = ["filter by the source type"])
    var sourceType:String? = null

    @CommandLine.Option(names = ["-n", "--noColors"], description = ["Disable ANSI colors"])
    var noColors = false

    @CommandLine.Option(names = ["--noGrouping"], description = ["Disable tasks grouping"])
    var noGrouping = false

    @CommandLine.Option(names = ["--groupingThreshold"], description = ["Grouping threshold"])
    var groupingThreshold = 0.05

    @CommandLine.Option(names = ["--noItems"], description = ["Hide items in groups"])
    var noItems = false

    @CommandLine.Option(names = ["-u", "--user"], description = ["Specific user"])
    var user:String? = null

    @CommandLine.Option(names = ["--daysBack"], description = ["Days back"])
    var daysBack:Long = 2

    override fun call(): Int {
        BVColorUtils.useColors = !noColors

        val sinceDateTime = ZonedDateTime.now().minusDays(daysBack)

        val taskGroups = taskService.getTaskGroups(
                TasksRequest(
                status = status,
                grouping = !noGrouping,
                groupingThreshold = groupingThreshold,
                since = sinceDateTime,
                user = user,
                sourceType = sourceType
                ))

        println("Listing work in '${bold(BVColorUtils.red(status))}' state.")
        val now = LocalDate.now()
        println("Activity" +
                (user?.let { " for ${bold(it)}" } ?: "") +
                " for the last ${bold(daysBack.toString())} days" +
                " (from ${bold(sinceDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE))}" +
                " to ${bold(now.format(DateTimeFormatter.ISO_LOCAL_DATE))})")
        println("")
        printTaskGroups(taskGroups)

        return 0
    }

    fun printTaskGroups(tasksGroup:List<BVDocument>) {
        tasksGroup.forEach { group->
            if(noItems) {
                describe(group)
            } else  {
                describe(group, 0)
            }
        }
    }

    fun describe(doc: BVDocument, level: Int) {
        println(" ".repeat(level*2) +
                ((if (level > 0) "|-> " else "") + describe(doc)))

        doc.subDocuments.forEach { subDoc ->
            describe(subDoc, level + 1)
        }
    }

    fun describe(task: BVDocument)
            = "${dateFormat.format(task.updated)} - " +
            "${task.status?.take(10)?.padEnd(10)} - " +
            "${BVColorUtils.red(task.title ?: "???")} : " +
            "${task.httpUrl}" +
            (task.operations.firstOrNull()?.let { " (${it.author}:${BVColorUtils.red(it.description)})" } ?: "")
}