package org.birdview.command

import org.birdview.web.ReportWebService
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(name = "web", mixinStandardHelpOptions = true,
        description = ["Runs web server."])
class WebServerCommand(
        private val reportWebService: ReportWebService
) : Callable<Int> {
    override fun call(): Int {
        reportWebService.run()
        readLine()
        return 0
    }
}