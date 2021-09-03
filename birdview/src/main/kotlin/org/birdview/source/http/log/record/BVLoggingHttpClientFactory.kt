package org.birdview.source.http.log.record

import org.birdview.config.BVFoldersConfig
import org.birdview.source.http.BVHttpClient
import org.birdview.source.http.BVHttpClientFactory
import org.birdview.utils.JsonDeserializer
import org.birdview.utils.remote.ApiAuth
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.util.function.Predicate

class BVLoggingHttpClientFactory(
    private val delegate: BVHttpClientFactory,
    private val jsonDeserializer: JsonDeserializer,
    foldersConfig: BVFoldersConfig
): BVHttpClientFactory {
    private val logFolder = foldersConfig.getHttpInteractionsLogFolder()
    private val log = LoggerFactory.getLogger(BVLoggingHttpClientFactory::class.java)
    init {
        log.info("BVLoggingHttpClientFactory created")
    }
    init {
        Files.createDirectories(logFolder).also {
            Files.walk(it)
                .sorted(Comparator.reverseOrder())
                .filter(Predicate.not(logFolder::equals))
                .forEach (Files::deleteIfExists)
        }
    }

    override fun getHttpClientAuthenticated(url: String, authProvider: () -> ApiAuth?): BVHttpClient =
        LoggingDelegateHttpClient(
            delegate.getHttpClientAuthenticated(url, authProvider),
            logFolder,
            jsonDeserializer)

    override fun getHttpClientUnauthenticated(url: String): BVHttpClient =
        LoggingDelegateHttpClient(
            delegate.getHttpClientUnauthenticated(url),
            logFolder,
            jsonDeserializer)
}
