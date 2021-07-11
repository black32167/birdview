package org.birdview.storage.file

import org.birdview.BVCacheNames.SOURCE_SECRET_CACHE_NAME
import org.birdview.config.BVFoldersConfig
import org.birdview.storage.BVAbstractSourceConfig
import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.utils.JsonDeserializer
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.stream.Collectors
import javax.inject.Named

@Named
open class BVFileSourceSecretsStorage(
        open val bvFoldersConfig: BVFoldersConfig,
        open val jsonDeserializer: JsonDeserializer
): BVSourceSecretsStorage {
    private val log = LoggerFactory.getLogger(BVSourceSecretsStorage::class.java)

    @Cacheable(SOURCE_SECRET_CACHE_NAME)
    override fun getConfigByName(sourceName: String): BVAbstractSourceConfig? =
            getSourceConfigs().find { it.sourceName == sourceName }

    @Cacheable(SOURCE_SECRET_CACHE_NAME)
    override fun <T: BVAbstractSourceConfig> getConfigByName(sourceName: String, configClass: Class<T>): T? =
            getConfigByName(sourceName) as? T

    override fun listSourceNames(): List<String> =
            getSourceConfigs().map { it.sourceName }

    @CacheEvict(SOURCE_SECRET_CACHE_NAME, allEntries = true)
    override fun create(config: BVAbstractSourceConfig) {
        bvFoldersConfig.sourcesSharedSecretsConfigsFolder.also { folder->
            Files.createDirectories(folder)
            jsonDeserializer.serialize(folder.resolve(config.sourceName), config)
        }
    }

    @CacheEvict(SOURCE_SECRET_CACHE_NAME, allEntries = true)
    override fun update(config: BVAbstractSourceConfig) {
            bvFoldersConfig.sourcesSharedSecretsConfigsFolder.resolve(config.sourceName).also { file ->
            Files.move(file, file.resolveSibling("${file}.bak"), StandardCopyOption.REPLACE_EXISTING)
            jsonDeserializer.serialize(file, config)
        }
    }

    @CacheEvict("sourcesConfig", allEntries = true)
    override fun delete(sourceName: String) {
        Files.delete(bvFoldersConfig.sourcesSharedSecretsConfigsFolder.resolve(sourceName))
    }

    private fun getSourceConfigs(): List<BVAbstractSourceConfig> = bvFoldersConfig.sourcesSharedSecretsConfigsFolder
            .takeIf { Files.isDirectory(it) }
            ?.let (Files::list)
            ?.filter { !it.toString().toLowerCase().endsWith(".bak") }
            ?.collect(Collectors.toList())
            ?.mapNotNull (this::deserialize)
            ?: emptyList()

    private fun deserialize(path: Path): BVAbstractSourceConfig? {
        try {
            return jsonDeserializer.deserialize(path)
        } catch (e: Exception) {
            log.error("", e)
            return null
        }
    }
}