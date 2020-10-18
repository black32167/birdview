package org.birdview.storage.file

import org.birdview.config.BVFoldersConfig
import org.birdview.storage.BVUserStorage
import org.birdview.storage.model.BVUserSettings
import org.birdview.utils.JsonDeserializer
import org.springframework.cache.annotation.Cacheable
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import javax.inject.Named

@Named
class BVFileUserStorage (
        private val bvFoldersConfig: BVFoldersConfig,
        private val jsonDeserializer: JsonDeserializer
) : BVUserStorage {
    companion object {
        val userSettingsFile = "user.json"
    }
    override fun create(userName:String, userSettings: BVUserSettings) {
        val userSettingsFile = getUserSettingsFile(userName)
        Files.createDirectories(userSettingsFile.parent)
        serialize(userSettingsFile, userSettings)
    }

    override fun update(userName:String, userSettings: BVUserSettings) {
        serialize(getUserSettingsFile(userName), userSettings)
    }

    override fun getUserSettings(userName: String): BVUserSettings =
            deserialize(getUserSettingsFile(userName))

    @Cacheable(BVFileUserSourceStorage.CACHE_NAME)
    override fun listUsers(): List<String> = Files.list(bvFoldersConfig.usersConfigFolder)
            .filter { Files.isDirectory(it) }
            .map { it.fileName.toString() }
            .collect(Collectors.toList())

    private fun serialize(file:Path, userSettings: BVUserSettings) {
        jsonDeserializer.serialize(file, userSettings)
    }

    private fun deserialize(file:Path): BVUserSettings =
        jsonDeserializer.deserialize(file, BVUserSettings::class.java)

    private fun getUserSettingsFile(userName: String) =
            bvFoldersConfig.getUserConfigFolder(userName).resolve(userSettingsFile)
}