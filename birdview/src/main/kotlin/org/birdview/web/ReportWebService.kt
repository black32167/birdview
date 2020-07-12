package org.birdview.web

import freemarker.template.Configuration
import org.birdview.BVTaskService
import org.birdview.model.ReportType
import org.birdview.request.TasksRequest
import org.glassfish.grizzly.http.server.HttpHandler
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.grizzly.http.server.Request
import org.glassfish.grizzly.http.server.Response
import java.io.OutputStreamWriter
import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Named

@Named
class ReportWebService(
        private val taskService: BVTaskService) {
    private val reportTemplatePath = "web/report.ftl"
    private val port = 8888
    private val freemarkerConfig = Configuration(Configuration.VERSION_2_3_29).apply {
        setClassForTemplateLoading(this::class.java, "/")
    }
    fun run() {
        val baseUrl = "http://localhost:${port}"
        println("Open $baseUrl")
        HttpServer.createSimpleServer(null, port)
                .apply {
                    serverConfiguration.addHttpHandler(object : HttpHandler() {
                        override fun service(request: Request, response: Response) {
                            // Refresh cache if requested
                            request.getParameter("refresh")
                                    ?.also { taskService.invalidateCache() }

                            val reportType = request.getParameter("report")
                                    ?.toUpperCase()
                                    ?.let { ReportType.valueOf(it) }
                                    ?: ReportType.LAST_DAY

                            val docs = taskService.getTaskGroups(buildTSRequest(reportType))
                                    .map(BVDocumentViewFactory::create)

                            response.apply {
                                contentType = "text/html"
                                freemarkerConfig.getTemplate(reportTemplatePath)
                                        .process(
                                                mapOf(
                                                        "reportTypes" to ReportType.values().map { it.name.toLowerCase() },
                                                        "docs" to docs,
                                                        "baseURL" to baseUrl,
                                                        "reportPath" to "report-${reportType}.ftl",
                                                        "format" to getFormat(reportType)),
                                                OutputStreamWriter(outputStream))
                            }
                        }
                    })
                }
            .start()
    }

    private fun buildTSRequest(reportType: ReportType) :TasksRequest {
        val sourceType = null
        val today = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        return when(reportType) {
            ReportType.LAST_DAY -> {
                val minusDays:Long = if(listOf(DayOfWeek.MONDAY, DayOfWeek.SUNDAY).contains(today.dayOfWeek))
                    today.dayOfWeek.ordinal - DayOfWeek.FRIDAY.ordinal.toLong()
                else
                    1L
                TasksRequest(
                        reportType = reportType,
                        grouping = false,
                        since = today.minusDays(minusDays),
                        user = null,
                        sourceType = sourceType)
            }
            ReportType.PLANNED -> TasksRequest(
                    reportType = reportType,
                    grouping = true,
                    since = null,
                    user = null,
                    sourceType = sourceType)
            ReportType.WORKED -> TasksRequest(
                    reportType = reportType,
                    grouping = true,
                    since = today.minusDays(10),
                    user = null,
                    sourceType = sourceType)
        }
    }

    private fun getFormat(reportType: ReportType): String = when(reportType) {
        ReportType.LAST_DAY -> "brief"
        else -> "long"
    }
}