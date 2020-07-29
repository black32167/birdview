package org.birdview

import org.birdview.command.BirdviewCommand
import org.birdview.command.TaskListCommand
import org.birdview.command.WebServerCommand
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import picocli.CommandLine

@Configuration
@ComponentScan
@EnableCaching
@SpringBootApplication
open class BirdviewConfiguration (
        var taskListCommand:TaskListCommand,
        var webServerCommand: WebServerCommand
) : CommandLineRunner {
    @Bean
    open fun cacheManager(): CacheManager? {
        return ConcurrentMapCacheManager("bv")
    }

    override fun run(vararg args: String?) {
        CommandLine(BirdviewCommand())
                .addSubcommand(taskListCommand)
                .addSubcommand(webServerCommand)
                .also { it.isCaseInsensitiveEnumValuesAllowed = true }
                .execute(*args)
    }
}