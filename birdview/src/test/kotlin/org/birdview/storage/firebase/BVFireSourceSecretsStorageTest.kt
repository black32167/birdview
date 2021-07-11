package org.birdview.storage.firebase

import org.birdview.firebase.AbstractFirebaseStorageTest
import org.birdview.storage.BVConfluenceConfig
import org.birdview.storage.BVGDriveConfig
import org.birdview.storage.BVGithubConfig
import org.birdview.storage.BVJiraConfig
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner
import javax.inject.Inject

@RunWith(SpringRunner::class)
class BVFireSourceSecretsStorageTest : AbstractFirebaseStorageTest() {
    private companion object {
        private const val SOURCE_NAME_GITHUB = "github-source-name"
        private const val SOURCE_NAME_JIRA = "jira-source-name"
        private const val SOURCE_NAME_CONFLUENCE = "confluence-source-name"
        private const val SOURCE_NAME_GDRIVE_1 = "gdrive-source-name-1"
        private const val SOURCE_NAME_GDRIVE_2= "gdrive-source-name-2"

        private val configs = listOf(
            BVGDriveConfig(
                sourceName = SOURCE_NAME_GDRIVE_1,
                clientId = "clientId1",
                clientSecret = "secret1",
                user = "user"
            ),
            BVGDriveConfig(
                sourceName = SOURCE_NAME_GDRIVE_2,
                clientId = "clientId2",
                clientSecret = "secret2",
                user = "user"
            ),
            BVConfluenceConfig(
                sourceName = SOURCE_NAME_CONFLUENCE,
                baseUrl = "confluence-url",
                token = "token",
                user = "user"
            ),
            BVJiraConfig(
                sourceName = SOURCE_NAME_JIRA,
                baseUrl = "jira-url",
                token = "token",
                user = "user"
            ),
            BVGithubConfig(
                sourceName = SOURCE_NAME_GITHUB,
                token = "token",
                user = "user"
            )
        )
    }

    @Inject
    private lateinit var storage: BVFireSourceSecretsStorage

    @Test
    fun testGetConfigByNameUntyped() {
        prepareTestConfigs()

        configs.forEach { expectedConfig->
            val config = storage.getConfigByName(expectedConfig.sourceName)
            assertNotNull(config)
        }
    }

    @Test
    fun testListSourceNames() {
        prepareTestConfigs()

        val sourceNames = storage.listSourceNames()

        val expectedNames = configs.map { it.sourceName }.toSet()
        assertEquals(expectedNames, sourceNames.toSet())
    }

    @Test
    fun testUpdate() {
        prepareTestConfigs()

        storage.update(BVGDriveConfig(
            sourceName = SOURCE_NAME_GDRIVE_2,
            clientId = "clientId3",
            clientSecret = "secret3",
            user = "user"
        ))

        val updatedConfig = storage.getConfigByName(SOURCE_NAME_GDRIVE_2, BVGDriveConfig::class.java)!!

        assertEquals("clientId3", updatedConfig.clientId)
        assertEquals("secret3", updatedConfig.clientSecret)
    }

    @Test
    fun testDelete() {
        prepareTestConfigs()

        storage.delete(SOURCE_NAME_GDRIVE_2)
        val sourceNames = storage.listSourceNames()

        assertFalse(sourceNames.contains(SOURCE_NAME_GDRIVE_2))
    }

    private fun prepareTestConfigs() {
        configs.forEach { storage.create(it) }
    }
}