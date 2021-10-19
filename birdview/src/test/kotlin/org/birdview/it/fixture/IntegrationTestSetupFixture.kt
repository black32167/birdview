package org.birdview.it.fixture

import org.birdview.security.PasswordUtils
import org.birdview.source.SourceType
import org.birdview.storage.BVUserSourceConfigStorage
import org.birdview.storage.BVUserStorage
import org.birdview.storage.model.BVUserSettings
import org.birdview.storage.model.source.config.BVUserSourceConfig
import javax.inject.Named

@Named
class IntegrationTestSetupFixture(
    private val userStorage: BVUserStorage,
    private val userSourcesStorage: BVUserSourceConfigStorage
) {
    companion object {
        val DEFAULT_SOURCE_NAME = "source1"
        val BV_USER1 = "bvUser1"
        val BV_USER2 = "bvUser2"
        val BV_USER_DISABLED = "bvDisabledUser"
    }

    fun setup() {
        createStandardUser(BV_USER1, createUser())
        createStandardUser(BV_USER2, createUser())
        createStandardUser(BV_USER_DISABLED, createUser(enabled = false))
    }

    private fun createStandardUser(bvUser: String, profile: BVUserSettings) {
        userStorage.update(bvUser, profile)
        userSourcesStorage.create(bvUser, createUserSource(sourceName = "${bvUser}_sourceName1"))
        userSourcesStorage.create(bvUser, createUserSource(sourceName = "${bvUser}_sourceName2"))
    }

    private fun createUserSource(
        sourceName:String,
        sourceType: SourceType = SourceType.JIRA): BVUserSourceConfig =
        BVUserSourceConfig(
            sourceName = sourceName,
            sourceType = sourceType,
            baseUrl = "https://base.url.com",
            sourceUserName = "${sourceName}_user",
            enabled = true,
            serializedSourceSecret = "{}"
        )

    private fun createUser(enabled:Boolean = true): BVUserSettings =
        BVUserSettings(
            email = "email@server.com",
            passwordHash = PasswordUtils.hash("12345"),
            enabled = enabled
        )

    fun getUserSources(bvUser: String): List<BVUserSourceConfig> =
        userSourcesStorage.listSources(bvUser)

    fun getUserNames(enabled:Boolean = true): List<String> = userStorage.listUserNames()
        .map { bvUser-> Pair(bvUser, userStorage.getUserSettings(bvUser)) }
        .filter { (name, profile) -> profile.enabled == enabled  }
        .map { (name, profile) -> name }
}