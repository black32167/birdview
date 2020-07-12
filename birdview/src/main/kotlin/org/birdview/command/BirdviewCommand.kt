package org.birdview.command

import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(helpCommand = true)
class BirdviewCommand() : Callable<Int> {
    override fun call(): Int {
        println("Use one of subcommands [list,web]")
        return 0
    }
}