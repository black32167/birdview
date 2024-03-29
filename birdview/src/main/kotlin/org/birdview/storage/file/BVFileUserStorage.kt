package org.birdview.storage.file

import org.birdview.BVCacheNames.USER_NAMES_CACHE
import org.birdview.BVCacheNames.USER_SETTINGS_CACHE
import org.birdview.BVProfiles
import org.birdview.config.BVFoldersConfig
import org.birdview.storage.BVUserSourceConfigStorage
import org.birdview.storage.BVUserStorage
import org.birdview.storage.model.BVUserSettings
import org.birdview.utils.JsonMapper
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Profile
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import javax.inject.Named

@Profile(BVProfiles.LOCAL)
@Named
class BVFileUserStorage (
    private val bvFoldersConfig: BVFoldersConfig,
    private val jsonMapper: JsonMapper,
    private val userSourceStorage: BVUserSourceConfigStorage
) : BVUserStorage {
    private val log = LoggerFactory.getLogger(BVFileUserStorage::class.java)

    companion object {
        val userSettingsFile = "user.json"
    }

    // TODO: not transactional
    @CacheEvict(USER_NAMES_CACHE, allEntries = true)
    override fun create(userName:String, userSettings: BVUserSettings) {
        val userSettingsFile = getUserSettingsFile(userName)
        if (Files.exists(userSettingsFile)) {
            throw BVUserStorage.UserStorageException("User already exists")
        }

        Files.createDirectories(userSettingsFile.parent)

        serialize(userSettingsFile, userSettings)
//
//        // Try to create user data sources
//        try {
//            sourceSecretsStorage.listSourceNames()
//                    .forEach { sourceName ->
//                        val sourceUserId = sourcesManager.getBySourceName(sourceName)
//                                ?.let { sourceManager ->
//                                    userSettings.email
//                                            ?.let { email ->
//                                                sourceManager.resolveSourceUserId(sourceName = sourceName, email = email)
//                                            }
//                                } ?: ""
//
//                        userSourceStorage.create(
//                            bvUser = userName,
//                            sourceName = sourceName,
//                            sourceUserName = sourceUserId
//                        )
//
//                    }
//        } catch (e: Exception) {
//            log.error("Error creating source links", e)
//        }
    }

    @CacheEvict(USER_SETTINGS_CACHE, allEntries = true)
    @Synchronized
    override fun update(userName:String, userSettings: BVUserSettings) {
        serialize(getUserSettingsFile(userName), userSettings)
    }

    @Cacheable(USER_SETTINGS_CACHE)
    @Synchronized
    override fun getUserSettings(userName: String): BVUserSettings =
            deserialize(getUserSettingsFile(userName))

    @CacheEvict(USER_SETTINGS_CACHE, allEntries = true)
    @Synchronized
    override fun updateUserStatus(userName: String, enabled: Boolean) {
        val userSettings = getUserSettings(userName)
        update(userName, userSettings.copy(enabled = enabled))
    }

    @CacheEvict(cacheNames = [USER_SETTINGS_CACHE, USER_NAMES_CACHE], allEntries = true)
    @Synchronized
    override fun delete(userName: String) {
        userSourceStorage.deleteAll(userName)
        Files.walk(bvFoldersConfig.getUserConfigFolder(userName))
                .sorted(Comparator.reverseOrder())
                .forEach(Files::delete)
    }

    override fun deleteGroup(bvUser: String, groupName: String) {
        val existingSettings = getUserSettings(bvUser)
        val updatedSettings = existingSettings.copy(workGroups = existingSettings.workGroups - groupName)
        update(bvUser, updatedSettings)
    }

    @Cacheable(USER_NAMES_CACHE)
    @Synchronized
    override fun listUserNames(): List<String> = Files.list(bvFoldersConfig.usersConfigFolder)
            .filter { Files.isDirectory(it) }
            .map { it.fileName.toString() }
            .collect(Collectors.toList())

    override fun getUsersInWorkGroup(workGroups: List<String>): List<String> =
        listUserNames()
            .filter { (getUserSettings(it).workGroups - workGroups).isNotEmpty() }

    private fun serialize(file:Path, userSettings: BVUserSettings) {
        jsonMapper.serialize(file, userSettings)
    }

    private fun deserialize(file:Path): BVUserSettings =
        jsonMapper.deserialize(file, BVUserSettings::class.java)

    private fun getUserSettingsFile(userName: String) =
            bvFoldersConfig.getUserConfigFolder(userName).resolve(userSettingsFile)
}
