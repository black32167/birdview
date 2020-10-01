package org.birdview.config.sources

import org.birdview.config.BVRuntimeConfig
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
open class BVSourcesConfigStorageImpl(
        open val bvRuntimeConfig: BVRuntimeConfig,
        open val jsonDeserializer: JsonDeserializer
):  BVSourcesConfigStorage {
    private val log = LoggerFactory.getLogger(BVSourcesConfigStorage::class.java)

    override fun <T: BVAbstractSourceConfig> getConfigsOfType(configClass: Class<T>):List<T> =
            getSourceConfigs()
                    .filter { configClass.isAssignableFrom(it.javaClass) }
                    .map { configClass.cast(it) }
                    .toList()

    override fun <T: BVAbstractSourceConfig> getConfigOfType(configClass: Class<T>): T? =
            getConfigsOfType(configClass).firstOrNull()

    @Cacheable("sourcesConfig")
    override fun getConfigByName(sourceName: String): BVAbstractSourceConfig? =
            getSourceConfigs().find { it.sourceName == sourceName }!!

    @Cacheable("sourcesConfig")
    override fun <T: BVAbstractSourceConfig> getConfigByName(sourceName: String, configClass: Class<T>): T? =
            getConfigByName(sourceName) as? T

    override fun listSourceNames(): List<String> =
            getSourceConfigs().map { it.sourceName }

    @CacheEvict("sourcesConfig", allEntries = true)
    override fun save(config: BVAbstractSourceConfig) {
        bvRuntimeConfig.sourcesConfigsFolder.also { folder->
            Files.createDirectories(folder)
            jsonDeserializer.serialize(folder.resolve(config.sourceName), config)
        }
    }

    @CacheEvict("sourcesConfig", allEntries = true)
    override fun update(config: BVAbstractSourceConfig) {
            bvRuntimeConfig.sourcesConfigsFolder.resolve(config.sourceName).also { file ->
            Files.move(file, file.resolveSibling("${file}.bak"), StandardCopyOption.REPLACE_EXISTING)
            jsonDeserializer.serialize(file, config)
        }
    }

    @CacheEvict("sourcesConfig", allEntries = true)
    override fun delete(sourceName: String) {
        Files.delete(bvRuntimeConfig.sourcesConfigsFolder.resolve(sourceName))
    }

    private fun getSourceConfigs(): List<BVAbstractSourceConfig> = bvRuntimeConfig.sourcesConfigsFolder
            .takeIf { Files.isDirectory(it) }
            ?.let (Files::list)
            ?.filter { !it.toString().toLowerCase().endsWith(".bak") }
            ?.collect(Collectors.toList())
            ?.mapNotNull (this::deserialize)
            ?: emptyList()

    private fun deserialize(path: Path):BVAbstractSourceConfig? {
        try {
            return jsonDeserializer.deserialize(path)
        } catch (e: Exception) {
            log.error("", e)
            return null
        }
    }
}