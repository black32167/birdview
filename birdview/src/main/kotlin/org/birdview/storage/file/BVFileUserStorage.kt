package org.birdview.storage.file

import org.birdview.BVCacheNames.USER_NAMES_CACHE
import org.birdview.BVCacheNames.USER_SETTINGS_CACHE
import org.birdview.config.BVFoldersConfig
import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.storage.BVSourcesManager
import org.birdview.storage.BVUserSourceStorage
import org.birdview.storage.BVUserStorage
import org.birdview.storage.model.BVUserSettings
import org.birdview.utils.JsonDeserializer
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import java.lang.Exception
import java.lang.RuntimeException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.CopyOnWriteArrayList
import java.util.stream.Collectors
import javax.inject.Named

@Named
class BVFileUserStorage (
        private val bvFoldersConfig: BVFoldersConfig,
        private val jsonDeserializer: JsonDeserializer,
        private val sourceSecretsStorage: BVSourceSecretsStorage,
        private val sourcesManager: BVSourcesManager,
        private val userSourceStorage: BVUserSourceStorage
) : BVUserStorage {
    private val log = LoggerFactory.getLogger(BVFileUserStorage::class.java)
    private val userCreatedListeners = CopyOnWriteArrayList<BVUserStorage.UserChangedListener>()

    companion object {
        val userSettingsFile = "user.json"
    }

    // TODO: not transactional
    @CacheEvict(USER_NAMES_CACHE, allEntries = true)
    override fun create(bvUserName:String, userSettings: BVUserSettings) {
        val userSettingsFile = getUserSettingsFile(bvUserName)
        if (Files.exists(userSettingsFile)) {
            throw BVUserStorage.UserStorageException("User already exists")
        }

        Files.createDirectories(userSettingsFile.parent)

        serialize(userSettingsFile, userSettings)

        // Try to create user data sources
        try {
            sourceSecretsStorage.listSourceNames()
                    .forEach { sourceName ->
                        val sourceUserId = sourcesManager.getBySourceName(sourceName)
                                ?.let { sourceManager ->
                                    userSettings.email
                                            ?.let { email ->
                                                sourceManager.resolveSourceUserId(sourceName = sourceName, email = email)
                                            }
                                } ?: ""

                        userSourceStorage.create(
                                bvUserName = bvUserName,
                                sourceName = sourceName,
                                sourceUserName = sourceUserId
                        )

                    }
        } catch (e: Exception) {
            log.error("Error creating source links", e)
        }
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

    override fun addUserCreatedListener(userChangedListener: BVUserStorage.UserChangedListener) {
        userCreatedListeners.add(userChangedListener)
    }

    @CacheEvict(cacheNames = [USER_SETTINGS_CACHE, USER_NAMES_CACHE], allEntries = true)
    @Synchronized
    override fun delete(bvUserName: String) {
        userSourceStorage.deleteAll(bvUserName)
        Files.walk(bvFoldersConfig.getUserConfigFolder(bvUserName))
                .sorted(Comparator.reverseOrder())
                .forEach(Files::delete)
    }

    @Cacheable(USER_NAMES_CACHE)
    @Synchronized
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