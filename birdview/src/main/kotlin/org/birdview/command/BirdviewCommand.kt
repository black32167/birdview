package org.birdview.command

import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command
class BirdviewCommand() : Callable<Int> {
    override fun call(): Int {
        throw UnsupportedOperationException()
    }
}