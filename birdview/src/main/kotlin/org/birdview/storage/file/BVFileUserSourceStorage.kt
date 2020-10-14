package org.birdview.storage.file

import org.birdview.config.BVFoldersConfig
import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.storage.BVUserSourceStorage
import org.birdview.storage.model.BVUserSourceConfig
import org.birdview.utils.JsonDeserializer
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.stream.Collectors.toList
import javax.inject.Named

@Named
class BVFileUserSourceStorage(
        private val bvFoldersConfig: BVFoldersConfig,
        private val jsonDeserializer: JsonDeserializer,
        private val sourceSecretsStorage: BVSourceSecretsStorage
): BVUserSourceStorage {
    companion object {
        const val CACHE_NAME = "userProfilesConfig"
    }

    @Cacheable(CACHE_NAME)
    override fun getBVUserNameBySourceUserName(userAlias: String?, sourceName: String): String =
            if (userAlias.isNullOrBlank()) {
                sourceSecretsStorage.getConfigByName(sourceName)?.user ?: throw java.lang.RuntimeException("Config not found for $sourceName")
            } else {
                jsonDeserializer
                        .deserialize(getSourceConfigFileName(userAlias, sourceName), BVUserSourceConfig::class.java)
                        .sourceUserName
            }

    @Cacheable(CACHE_NAME)
    override fun getSourceProfile(bvUserName: String, sourceName: String): BVUserSourceConfig =
            deserialize(getSourceConfigFileName(bvUserName = bvUserName, sourceName = sourceName))

    override fun listUserSources(userName: String): List<String> =
            Files.list(getUserSourcesFolder(userName))
                    .map { it.toFile() }
                    .filter { it.extension == "json" }
                    .map { it.name.substringBeforeLast(".") }
                    .collect(toList())

    @CacheEvict(CACHE_NAME, allEntries = true)
    override fun create(bvUserName: String, sourceName: String, sourceUserName:String) {
        serialize(getSourceConfigFileName(bvUserName = bvUserName, sourceName = sourceName), BVUserSourceConfig(sourceUserName, false))
    }

    @CacheEvict(CACHE_NAME, allEntries = true)
    override fun update(bvUserName: String, sourceName: String, userProfileSourceConfig: BVUserSourceConfig) {
        val file = getSourceConfigFileName(bvUserName = bvUserName, sourceName = sourceName)
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
