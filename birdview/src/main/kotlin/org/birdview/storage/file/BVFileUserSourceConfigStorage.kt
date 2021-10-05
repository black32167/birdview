package org.birdview.storage.file

import org.birdview.BVCacheNames.USER_SOURCE_CACHE
import org.birdview.BVProfiles
import org.birdview.config.BVFoldersConfig
import org.birdview.storage.BVUserSourceConfigStorage
import org.birdview.storage.model.source.config.BVUserSourceConfig
import org.birdview.utils.JsonDeserializer
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Profile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.stream.Collectors.toList
import javax.inject.Named

@Profile(BVProfiles.LOCAL)
@Named
class BVFileUserSourceConfigStorage(
        private val bvFoldersConfig: BVFoldersConfig,
        private val jsonDeserializer: JsonDeserializer
): BVUserSourceConfigStorage {

    @Cacheable(USER_SOURCE_CACHE)
    @Synchronized
    override fun getSource(bvUser: String, sourceName: String): BVUserSourceConfig =
            deserialize(getSourceConfigFileName(bvUserName = bvUser, sourceName = sourceName))

    @Synchronized
    override fun listSourceNames(bvUser: String): List<String> =
            Files.list(getUserSourcesFolder(bvUser))
                    .map { it.toFile() }
                    .filter { it.extension == "json" }
                    .map { it.name.substringBeforeLast(".") }
                    .collect(toList())

    @Synchronized
    @CacheEvict(USER_SOURCE_CACHE, allEntries = true)
    override fun delete(bvUser: String, sourceName: String) {
        Files.delete(getSourceConfigFileName(bvUserName = bvUser, sourceName = sourceName))
    }

    @Synchronized
    @CacheEvict(USER_SOURCE_CACHE, allEntries = true)
    override fun deleteAll(bvUser: String) {
        listSourceNames(bvUser).forEach { sourceName ->
            Files.delete(getSourceConfigFileName(bvUserName = bvUser, sourceName = sourceName))
        }
    }

    @Cacheable(cacheNames = [USER_SOURCE_CACHE], key = "'sc-'.concat(#bvUser)" )
    override fun listSources(bvUser: String): List<BVUserSourceConfig> {
        return listSourceNames(bvUser).map { getSource(bvUser, it) }
    }

    @CacheEvict(USER_SOURCE_CACHE, allEntries = true)
    @Synchronized
    override fun create(bvUser: String, sourceConfig: BVUserSourceConfig) {
        serialize(
            getSourceConfigFileName(bvUserName = bvUser, sourceName = sourceConfig.sourceName),
            sourceConfig = sourceConfig
        )
    }

    @CacheEvict(USER_SOURCE_CACHE, allEntries = true)
    @Synchronized
    override fun update(bvUser: String, sourceConfig: BVUserSourceConfig) {
        val file = getSourceConfigFileName(bvUserName = bvUser, sourceName = sourceConfig.sourceName)
        Files.move(file, file.resolveSibling("${file}.bak"), StandardCopyOption.REPLACE_EXISTING)
        serialize(file, sourceConfig)
    }

    private fun serialize(file: Path, sourceConfig: BVUserSourceConfig) {
        jsonDeserializer.serialize(file, sourceConfig)
    }

    private fun deserialize(file: Path) =
        jsonDeserializer.deserialize<BVUserSourceConfig>(file)

    private fun getSourceConfigFileName(bvUserName: String, sourceName: String) =
            getUserSourcesFolder(bvUserName).resolve("${sourceName}.json")

    private fun getUserSourcesFolder(userAlias: String) =
            bvFoldersConfig.getUserSourceConfigsFolder(userAlias)
}
