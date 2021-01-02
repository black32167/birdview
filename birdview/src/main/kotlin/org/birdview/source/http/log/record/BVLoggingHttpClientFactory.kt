package org.birdview.source.http.log.record

import org.birdview.config.BVFoldersConfig
import org.birdview.source.http.BVHttpClient
import org.birdview.source.http.BVHttpClientFactory
import org.birdview.utils.JsonDeserializer
import org.birdview.utils.remote.ApiAuth
import java.nio.file.Files
import java.nio.file.Paths
import java.util.function.Predicate
import javax.annotation.PostConstruct

class BVLoggingHttpClientFactory(
    private val delegate: BVHttpClientFactory,
    private val jsonDeserializer: JsonDeserializer,
    foldersConfig: BVFoldersConfig
): BVHttpClientFactory {
    private val logFolder = foldersConfig.getHttpInteractionsLogFolder()

    init {
        Files.createDirectories(logFolder).also {
            Files.walk(it)
                .sorted(Comparator.reverseOrder())
                .filter(Predicate.not(logFolder::equals))
                .forEach (Files::deleteIfExists)
        }
    }

    override fun getHttpClient(url: String, authProvider: () -> ApiAuth?): BVHttpClient =
        LoggingDelegateHttpClient(
            delegate.getHttpClient(url, authProvider),
            logFolder,
            jsonDeserializer)
}
