package org.birdview.command

import org.birdview.web.ReportWebService
import picocli.CommandLine
import java.util.concurrent.Callable
import javax.inject.Named

@CommandLine.Command(name = "web", mixinStandardHelpOptions = true,
        description = ["Runs web server."])
@Named
class WebServerCommand(
        private val reportWebService: ReportWebService
) : Callable<Int> {
    @CommandLine.Option(names = ["-p", "--port"], description = ["Web server port"])
    var webPort:Int = 8888

    override fun call(): Int {
        reportWebService.runWebServer(webPort)
        readLine()
        return 0
    }
}