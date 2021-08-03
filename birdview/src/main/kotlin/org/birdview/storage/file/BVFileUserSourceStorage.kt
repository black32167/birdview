package org.birdview.storage.file

import org.birdview.BVCacheNames.USER_SOURCE_CACHE
import org.birdview.BVProfiles
import org.birdview.config.BVFoldersConfig
import org.birdview.storage.BVAbstractSourceConfig
import org.birdview.storage.BVUserSourceStorage
import org.birdview.storage.model.BVUserSourceConfig
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
    override fun getSourceProfile(bvUser: String, sourceName: String): BVUserSourceConfig =
            deserialize(getSourceConfigFileName(bvUserName = bvUser, sourceName = sourceName))

    @Synchronized
    override fun listUserSources(bvUser: String): List<String> =
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
        listUserSources(bvUser).forEach { sourceName ->
            Files.delete(getSourceConfigFileName(bvUserName = bvUser, sourceName = sourceName))
        }
    }

    @Cacheable(cacheNames = [USER_SOURCE_CACHE], key = "sc:#bvUser" )
    override fun listUserSourceProfiles(bvUser: String): List<BVUserSourceConfig> {
        return listUserSources(bvUser).map { getSourceProfile(bvUser, it) }
    }

    @CacheEvict(USER_SOURCE_CACHE, allEntries = true)
    @Synchronized
    override fun create(bvUser: String, sourceName: String, sourceUserName:String, bvSourceAccessConfig: BVAbstractSourceConfig?) {
        serialize(getSourceConfigFileName(bvUserName = bvUser, sourceName = sourceName),
            BVUserSourceConfig(
                sourceName = sourceName, sourceUserName = sourceUserName, enabled = "" != sourceUserName, sourceConfig = bvSourceAccessConfig))
    }

    @CacheEvict(USER_SOURCE_CACHE, allEntries = true)
    @Synchronized
    override fun update(bvUser: String, userProfileSourceConfig: BVUserSourceConfig) {
        val file = getSourceConfigFileName(bvUserName = bvUser, sourceName = userProfileSourceConfig.sourceName)
        Files.move(file, file.resolveSibling("${file}.bak"), StandardCopyOption.REPLACE_EXISTING)
        serialize(file, userProfileSourceConfig)
    }

    private fun serialize(file: Path, userProfileSourceConfig: BVUserSourceConfig) {
        jsonDeserializer.serialize(file, userProfileSourceConfig)
    }

    private fun deserialize(file: Path) =
        jsonDeserializer.deserialize<BVUserSourceConfig>(file)

    private fun getSourceConfigFileName(bvUserName: String, sourceName: String) =
            getUserSourcesFolder(bvUserName).resolve("${sourceName}.json")

    private fun getUserSourcesFolder(userAlias: String) =
            bvFoldersConfig.getUserSourceConfigsFolder(userAlias)
}
