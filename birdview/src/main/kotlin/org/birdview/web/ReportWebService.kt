package org.birdview.web

import org.birdview.analysis.BVDocument
import org.glassfish.grizzly.http.server.HttpHandler
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.grizzly.http.server.Request
import org.glassfish.grizzly.http.server.Response
import java.io.PrintWriter
import javax.inject.Named

@Named
class ReportWebService() {
    private val reportTemplatePath = "/web/report.html.template"
    private val port = 8888
    fun run(docs: List<BVDocument>) {
        println("Open http://localhost:${port}")
        HttpServer.createSimpleServer(null, port)
                .apply {
                    serverConfiguration.addHttpHandler(object : HttpHandler() {
                        override fun service(request: Request, response: Response) {
                            response.apply {
                                contentType = "text/html"
                                val reportContent = this::class.java.getResource(reportTemplatePath).readText()
                                PrintWriter(outputStream).use { writer ->
                                    writer.println(reportContent)
                                }
                            }
                        }
                    })
                }
            .start()
    }
}