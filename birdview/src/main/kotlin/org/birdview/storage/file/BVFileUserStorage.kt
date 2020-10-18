package org.birdview.storage.file

import org.birdview.BVCacheNames.USER_NAMES_CACHE
import org.birdview.BVCacheNames.USER_SETTINGS_CACHE
import org.birdview.config.BVFoldersConfig
import org.birdview.storage.BVUserStorage
import org.birdview.storage.model.BVUserSettings
import org.birdview.utils.JsonDeserializer
import org.springframework.cache.annotation.CacheEvict
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
    @CacheEvict(USER_NAMES_CACHE, allEntries = true)
    override fun create(userName:String, userSettings: BVUserSettings) {
        val userSettingsFile = getUserSettingsFile(userName)
        Files.createDirectories(userSettingsFile.parent)
        serialize(userSettingsFile, userSettings)
    }

    @CacheEvict(USER_SETTINGS_CACHE, allEntries = true)
    override fun update(userName:String, userSettings: BVUserSettings) {
        serialize(getUserSettingsFile(userName), userSettings)
    }

    @Cacheable(USER_SETTINGS_CACHE)
    override fun getUserSettings(userName: String): BVUserSettings =
            deserialize(getUserSettingsFile(userName))

    @CacheEvict(USER_SETTINGS_CACHE, allEntries = true)
    override fun updateUserStatus(userName: String, enabled: Boolean) {
        val userSettings = getUserSettings(userName)
        update(userName, userSettings.copy(enabled = enabled))
    }

    @Cacheable(USER_NAMES_CACHE)
    override fun listUserNames(): List<String> = Files.list(bvFoldersConfig.usersConfigFolder)
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