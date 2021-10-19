package org.birdview.it.firebase

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.analysis.BVDocumentOperation
import org.birdview.analysis.BVDocumentOperationType
import org.birdview.it.fixture.IntegrationTestSetupFixture
import org.birdview.model.*
import org.birdview.storage.BVDocumentStorage
import org.birdview.storage.model.source.config.BVUserSourceConfig
import org.junit.Before
import org.junit.Test
import java.time.OffsetDateTime
import java.util.*
import javax.inject.Inject

class BVFireDocumentStorageIntegrationTest: AbstractFirebaseStorageTest() {
    private val now = OffsetDateTime.now()
    private val past = now.minusDays(1)

    @Inject
    private lateinit var documentStorage: BVDocumentStorage

    @Inject
    private lateinit var setupFixture: IntegrationTestSetupFixture

    @Before
    fun init() {
        setupFixture.setup()

    }
//    userSourceStorage

    @Test
    fun testFindDocumentsWhenUserMatches() {

        val (user1, user2) = setupFixture.getUserNames()
        val user1Source  = setupFixture.getUserSources(user1).first()
        val doc1 = createDoc(source = user1Source)
        documentStorage.updateDocument(doc1, user1)

        val user2Source  = setupFixture.getUserSources(user2).first()
        val doc2 = createDoc(source = user2Source)
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

        val user = setupFixture.getUserNames().first()
        val (user1Source, user2Source) = setupFixture.getUserSources(user)

        val doc1 = createDoc(updated = now, source = user1Source)
        documentStorage.updateDocument(doc1, user)

        val doc2 = createDoc(source = user2Source)
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

    @Test
    fun testGetReferringDocuments() {

        val user = setupFixture.getUserNames().first()
        val user1Source = setupFixture.getUserSources(user).first()

        val doc1 = createDoc(source = user1Source, refIds= listOf("doc2"))
        documentStorage.updateDocument(doc1, user)
        documentStorage.updateDocument(createDoc(updated = now, source = user1Source, ids = listOf("doc2")), user)

        val referringDocs = documentStorage.getReferringDocuments(setOf("doc2"))

        assertThat(referringDocs).hasSize(1)
        assertThat(referringDocs.first().internalId).isEqualTo(doc1.internalId)
    }

    @Test
    fun testRemoveExistingExternalIds() {
        val user = setupFixture.getUserNames().first()
        val user1Source = setupFixture.getUserSources(user).first()

        documentStorage.updateDocument(createDoc(updated = now, source = user1Source, ids = listOf("doc1")), user)

        val missedIds = documentStorage.removeExistingExternalIds(listOf("doc2", "doc1"))

        assertThat(missedIds).containsExactlyInAnyOrder("doc2")
    }

    @Test
    fun testRemoveExistingExternalIdsWithEmptyInput() {
        val user = setupFixture.getUserNames().first()
        val user1Source = setupFixture.getUserSources(user).first()

        documentStorage.updateDocument(createDoc(updated = now, source = user1Source, ids = listOf("doc1")), user)

        val missedIds = documentStorage.removeExistingExternalIds(listOf())

        assertThat(missedIds).isEmpty()
    }

    @Test
    fun testRemoveExistingExternalIdsWithMoreThenTenItems() {
        val user = setupFixture.getUserNames().first()
        val user1Source = setupFixture.getUserSources(user).first()

        documentStorage.updateDocument(createDoc(updated = now, source = user1Source, ids = listOf("doc1")), user)

        assertThatThrownBy {
            documentStorage.removeExistingExternalIds(
                listOf(
                    "doc1",
                    "doc2",
                    "doc3",
                    "doc4",
                    "doc5",
                    "doc6",
                    "doc7",
                    "doc8",
                    "doc9",
                    "doc10",
                    "doc11"
                )
            )
        }

    }

    @Test
    fun testRemoveExistingExternalIdsWithMoreThenTenDuplicatedItems() {
        val user = setupFixture.getUserNames().first()
        val user1Source = setupFixture.getUserSources(user).first()

        documentStorage.updateDocument(createDoc(updated = now, source = user1Source, ids = listOf("doc1")), user)

        val missedIds = documentStorage.removeExistingExternalIds(listOf("doc1","doc2", "doc3","doc4", "doc5","doc6", "doc7","doc8", "doc9","doc10", "doc1"))

        assertThat(missedIds).hasSize(9)
    }

    @Test
    fun testGetDocuments() {
        val user = setupFixture.getUserNames().first()
        val user1Source = setupFixture.getUserSources(user).first()

        val savingDoc = createDoc(updated = now, source = user1Source, ids = listOf("doc1"))
        documentStorage.updateDocument(savingDoc, user)

        val retrievedDocs = documentStorage.getDocuments(listOf("doc2", "doc1"))

        assertThat(retrievedDocs).hasSize(1)
        assertThat(retrievedDocs.first().internalId).isEqualTo(savingDoc.internalId)
    }

    private fun createDoc(updated: OffsetDateTime? = now,
                          source: BVUserSourceConfig,
                          refIds : List<String> = listOf(),
                          ids : List<String> = listOf()
    ):BVDocument {
        val refs = refIds.map { BVDocumentRef(docId = BVDocumentId(it)) }
        return BVDocument(
            ids = ids.map { BVDocumentId(it) }.toSet(),
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
            sourceName = source.sourceName,
            refs = refs,
            internalId = UUID.randomUUID().toString()
        )
    }
}
