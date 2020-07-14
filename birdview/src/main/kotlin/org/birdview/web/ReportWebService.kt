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
    class ReportLink(val reportUrl:String, val reportName:String)
    private val reportTemplatePath = "web/report.ftl"
    private val freemarkerConfig = Configuration(Configuration.VERSION_2_3_29).apply {
        setClassForTemplateLoading(this::class.java, "/")
    }
    fun runWebServer(port: Int) {
        val baseUrl = "http://localhost:${port}"
        println("Open $baseUrl")
        HttpServer.createSimpleServer(null, port)
                .apply {
                    serverConfiguration.addHttpHandler(object : HttpHandler() {
                        override fun service(request: Request, response: Response) {
                            // Refresh cache if requested
                            request.getParameter("refresh")
                                    ?.also { taskService.invalidateCache() }

                            val tsRequest = buildTSRequest(request)
                            val docs = taskService.getTaskGroups(tsRequest)
                                    .map(BVDocumentViewFactory::create)

                            response.apply {
                                contentType = "text/html"
                                freemarkerConfig.getTemplate(reportTemplatePath)
                                        .process(
                                                mapOf(
                                                        "reportLinks" to ReportType.values()
                                                                .map { ReportLink(
                                                                        reportUrl = reportUrl(it, tsRequest, baseUrl),
                                                                        reportName = it.name.toLowerCase().capitalize()) },
                                                        "user" to tsRequest.user,
                                                        "docs" to docs,
                                                        "baseURL" to baseUrl,
                                                        "reportPath" to "report-${tsRequest.reportType}.ftl",
                                                        "format" to getFormat(tsRequest.reportType)),
                                                OutputStreamWriter(outputStream))
                            }
                        }
                    })
                }
            .start()
    }

    private fun reportUrl(reportType: ReportType, tsRequest: TasksRequest, baseUrl: String): String {
        return "${baseUrl}?report=${reportType.name.toLowerCase()}" +
                (tsRequest.user?.let { "&user=${it}" } ?: "")
    }

    private fun buildTSRequest(request: Request) :TasksRequest {
        val sourceType = null
        val user = request.getParameter("user")
        val reportType = request.getParameter("report")
                ?.toUpperCase()
                ?.let { ReportType.valueOf(it) }
                ?: ReportType.LAST_DAY
        val today = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        return when(reportType) {
            ReportType.LAST_DAY -> {
                val minusDays:Long = when(today.dayOfWeek) {
                    DayOfWeek.MONDAY -> 3L
                    DayOfWeek.SUNDAY -> 2L
                    else -> 1L
                }
                TasksRequest(
                        reportType = reportType,
                        grouping = false,
                        since = today.minusDays(minusDays),
                        user = user,
                        sourceType = sourceType)
            }
            ReportType.PLANNED -> TasksRequest(
                    reportType = reportType,
                    grouping = true,
                    since = null,
                    user = user,
                    sourceType = sourceType)
            ReportType.WORKED -> TasksRequest(
                    reportType = reportType,
                    grouping = true,
                    since = today.minusDays(10),
                    user = user,
                    sourceType = sourceType)
        }
    }

    private fun getFormat(reportType: ReportType): String = when(reportType) {
        ReportType.LAST_DAY -> "brief"
        else -> "long"
    }
}