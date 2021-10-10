package org.birdview.it.firebase

import org.assertj.core.api.Assertions.assertThat
import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.analysis.BVDocumentOperation
import org.birdview.analysis.BVDocumentOperationType
import org.birdview.it.fixture.IntegrationTestSetupFixture
import org.birdview.model.*
import org.birdview.storage.BVDocumentStorage
import org.birdview.storage.model.source.config.BVUserSourceConfig
import org.junit.Test
import java.time.OffsetDateTime
import javax.inject.Inject

class BVFireDocumentStorageIntegrationTest: AbstractFirebaseStorageTest() {

    @Inject
    private lateinit var documentStorage: BVDocumentStorage

    @Inject
    private lateinit var setupFixture: IntegrationTestSetupFixture

//    userSourceStorage

    @Test
    fun testFindDocumentsWhenUserMatches() {
        setupFixture.setup()

        val now = OffsetDateTime.now()
        val past = now.minusDays(1)
        val future = now.plusDays(1)
        val (user1, user2) = setupFixture.getUsers()
            .filter { (name, profile) -> profile.enabled }
            .map { (name, profile) -> name }

        val user1Source  = setupFixture.getUserSources(user1).first()
        val doc1 = createDoc(updated = now, source = user1Source)
        documentStorage.updateDocument(doc1, user1)

        val user2Source  = setupFixture.getUserSources(user2).first()
        val doc2 = createDoc(updated = now, source = user2Source)
        documentStorage.updateDocument(doc2, user2);

        val foundDocs = documentStorage.findDocuments(BVDocumentFilter(
            updatedPeriod = TimeIntervalFilter(after = past),
            docStatuses = listOf(BVDocumentStatus.PROGRESS),
            grouping = false,
            userFilter = UserFilter (userAlias = user1, UserRole.IMPLEMENTOR),
            sourceName = user1Source.sourceName
        ))

        assertThat(foundDocs).hasSize(1)
    }

    @Test
    fun testFindDocumentsWhenSourceTypeMatches() {
        setupFixture.setup()

        val now = OffsetDateTime.now()
        val past = now.minusDays(1)
        val future = now.plusDays(1)

        val user = setupFixture.getUsers()
            .filter { (name, profile) -> profile.enabled }
            .map { (name, profile) -> name }
            .first()
        val (user1Source, user2Source) = setupFixture.getUserSources(user)

        val doc1 = createDoc(updated = now, source = user1Source)
        documentStorage.updateDocument(doc1, user)

        val doc2 = createDoc(updated = now,  source = user2Source)
        documentStorage.updateDocument(doc2, user)

        val foundDocs = documentStorage.findDocuments(BVDocumentFilter(
            updatedPeriod = TimeIntervalFilter(after = past),
            docStatuses = listOf(BVDocumentStatus.PROGRESS),
            grouping = false,
            userFilter = UserFilter (userAlias = user, UserRole.IMPLEMENTOR),
            sourceName = user1Source.sourceName
        ))

        assertThat(foundDocs).hasSize(1)
        assertThat(foundDocs.first().sourceName).isEqualTo(user1Source.sourceName)
    }

    private fun createDoc(updated: OffsetDateTime?,
                          source: BVUserSourceConfig) =
        BVDocument(
            ids = setOf(BVDocumentId("sourceDocId")),
            title = "Title",
            key = "key",
            httpUrl = "http://url",
            status = BVDocumentStatus.PROGRESS,
            operations = listOf(
                BVDocumentOperation(
                    description = "description",
                    author = source.sourceUserName,
                    created = updated,
                    sourceName = source.sourceName,
                    type = BVDocumentOperationType.UPDATE
                ),
                BVDocumentOperation(
                    description = "description",
                    author = source.sourceUserName,
                    created = updated,
                    sourceName = source.sourceName,
                    type = BVDocumentOperationType.UPDATE
                )
            ),
            sourceName = source.sourceName
        )
}
