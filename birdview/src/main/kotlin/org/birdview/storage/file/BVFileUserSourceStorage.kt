package org.birdview.storage.file

import org.birdview.BVCacheNames.USER_SOURCE_CACHE
import org.birdview.BVProfiles
import org.birdview.config.BVFoldersConfig
import org.birdview.storage.BVUserSourceStorage
import org.birdview.storage.model.BVSourceConfig
import org.birdview.utils.JsonDeserializer
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Profile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.stream.Collectors.toList
import javax.inject.Named

@Profile(BVProfiles.FILESTORE)
@Named
class BVFileUserSourceStorage(
        private val bvFoldersConfig: BVFoldersConfig,
        private val jsonDeserializer: JsonDeserializer
): BVUserSourceStorage {

    @Cacheable(USER_SOURCE_CACHE)
    @Synchronized
    override fun getSource(bvUser: String, sourceName: String): BVSourceConfig =
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
    override fun listSourceProfiles(bvUser: String): List<BVSourceConfig> {
        return listSourceNames(bvUser).map { getSource(bvUser, it) }
    }

    @CacheEvict(USER_SOURCE_CACHE, allEntries = true)
    @Synchronized
    override fun create(bvUser: String, sourceName: String, sourceUserName: String) {
        serialize(getSourceConfigFileName(bvUserName = bvUser, sourceName = sourceName),
            BVSourceConfig(
                sourceName = sourceName, sourceUserName = sourceUserName, enabled = "" != sourceUserName))
    }

    @CacheEvict(USER_SOURCE_CACHE, allEntries = true)
    @Synchronized
    override fun update(bvUser: String, sourceConfig: BVSourceConfig) {
        val file = getSourceConfigFileName(bvUserName = bvUser, sourceName = sourceConfig.sourceName)
        Files.move(file, file.resolveSibling("${file}.bak"), StandardCopyOption.REPLACE_EXISTING)
        serialize(file, sourceConfig)
    }

    private fun serialize(file: Path, userProfileSourceConfig: BVSourceConfig) {
        jsonDeserializer.serialize(file, userProfileSourceConfig)
    }

    private fun deserialize(file: Path) =
        jsonDeserializer.deserialize<BVSourceConfig>(file)

    private fun getSourceConfigFileName(bvUserName: String, sourceName: String) =
            getUserSourcesFolder(bvUserName).resolve("${sourceName}.json")

    private fun getUserSourcesFolder(userAlias: String) =
            bvFoldersConfig.getUserSourceConfigsFolder(userAlias)
}
