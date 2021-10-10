package org.birdview.web.explore

import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.model.BVDocumentRef
import org.birdview.model.BVDocumentStatus
import org.birdview.model.RelativeHierarchyType
import org.birdview.source.SourceType
import org.birdview.storage.memory.BVDocumentPredicate
import org.birdview.storage.memory.BVInMemoryDocumentStorage
import org.birdview.utils.TestConfig
import org.birdview.web.explore.model.BVDocumentViewTreeNode
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit4.SpringRunner
import java.time.OffsetDateTime
import javax.inject.Inject

@Import(TestConfig::class)
@SpringBootTest("spring.main.allow-bean-definition-overriding=true")
@RunWith(SpringRunner::class)
class DocumentTreeBuilderTest {
    private val BV_USER = "bvUser"
    private var TIME_INSTANT = OffsetDateTime.now()
    private val documentStorage = BVInMemoryDocumentStorage(mock(BVDocumentPredicate::class.java))

    @Inject
    private lateinit var documentTreeBuilder: DocumentTreeBuilder

    @Before
    fun init() {
//        Mockito.`when`(docViewFactory.create(Mockito.any())).thenAnswer { invocation->
//            val doc = invocation.getArgumentAt(0, BVDocument::class.java)
//            BVDocumentView()
//        }

//        documentTreeBuilder = DocumentTreeBuilder(docViewFactory)
    }

    @Test
    fun testSimpleTreeBuilt() {
        val PARENT_ID = "parentId"
        val CHILDREN_ID = "childrenId"
        val GRANDCHILDREN_ID = "grandChildrenId"
        val docs = persistDocs(
                doc(listOf(PARENT_ID), SourceType.JIRA),
                doc(listOf(CHILDREN_ID), SourceType.JIRA, listOf(docRef(PARENT_ID, RelativeHierarchyType.LINK_TO_PARENT))),
                doc(listOf(GRANDCHILDREN_ID), SourceType.GDRIVE, listOf(docRef(CHILDREN_ID, RelativeHierarchyType.LINK_TO_PARENT)))
        )
        val views = documentTreeBuilder.buildTree(BV_USER, docs, documentStorage)
        Assert.assertEquals(1, views.size)
        Assert.assertEquals(1, views.first().subNodes.size)
        Assert.assertEquals(1, views.first().subNodes.first().subNodes.size)
        Assert.assertEquals(0, views.first().subNodes.first().subNodes.first().subNodes.size)
    }

    @Test
    fun testCyclicDependenciesDoNotCreateCycleInTree() {
        val docs = persistDocs(
                doc(listOf("id1", "id1Alt"), SourceType.JIRA, listOf(docRef("id2"))),
                doc(listOf("id2"), SourceType.JIRA, listOf(docRef("id3"))),
                doc(listOf("id3"), SourceType.JIRA, listOf(docRef("id1Alt")))
        )
        val tree = documentTreeBuilder.buildTree(BV_USER, docs, documentStorage)

        assertEquals(1, tree.size)
        val uniqueDoc = tree.first()
        assertFalse(uniqueDoc.alternativeDocs.any() { it.ids.contains(uniqueDoc.internalId) })
        tree.forEach { assertNoCycles(it, mutableSetOf()) }
    }

    @Test
    fun testCyclicDependencie2DoNotCreateCycleInTree() {
        val docs = persistDocs(
                doc(listOf("id0"), SourceType.JIRA, listOf(docRef("id1", RelativeHierarchyType.LINK_TO_CHILD))),
                doc(listOf("id1"), SourceType.JIRA, listOf(docRef("id2", RelativeHierarchyType.LINK_TO_CHILD), docRef("id3", RelativeHierarchyType.LINK_TO_CHILD))),
                doc(listOf("id2"), SourceType.JIRA, listOf(docRef("id1", RelativeHierarchyType.LINK_TO_CHILD))),
                doc(listOf("id3"), SourceType.JIRA, listOf(docRef("id1", RelativeHierarchyType.LINK_TO_CHILD), docRef("id2", RelativeHierarchyType.LINK_TO_CHILD)))
        )
        val tree = documentTreeBuilder.buildTree(BV_USER, docs, documentStorage)

        assertFalse(tree.isEmpty())
        assertTrue(tree.first().doc.ids.contains("id0"))
        assertFalse(tree.first().alternativeDocs.any() { it.ids.contains("id3") })
        tree.forEach { assertNoCycles(it, mutableSetOf()) }
    }

    @Test
    fun testDirectedCyclicDependenciesDoNotCreateCycleInTree() {
        val docs = persistDocs(
                doc(listOf("id1", "id1Alt"), SourceType.JIRA, listOf(docRef("id2", RelativeHierarchyType.LINK_TO_CHILD))),
                doc(listOf("id2"), SourceType.JIRA, listOf(docRef("id3", RelativeHierarchyType.LINK_TO_CHILD))),
                doc(listOf("id3"), SourceType.JIRA, listOf(docRef("id1Alt", RelativeHierarchyType.LINK_TO_CHILD)))
        )
        val tree = documentTreeBuilder.buildTree(BV_USER, docs, documentStorage)

        assertFalse(tree.isEmpty())
        tree.forEach { assertNoCycles(it, mutableSetOf()) }
    }

    @Test
    fun testDirectedBackwardDependency() {
        val docs = persistDocs(
                doc(listOf("id1", "id1Alt"), SourceType.JIRA, listOf(docRef("id2", RelativeHierarchyType.LINK_TO_CHILD))),
                doc(listOf("id2"), SourceType.JIRA, listOf()),
                doc(listOf("id3"), SourceType.JIRA, listOf(docRef("id1Alt", RelativeHierarchyType.LINK_TO_CHILD)))
        )
        val tree = documentTreeBuilder.buildTree(BV_USER, docs, documentStorage)

        assertFalse(tree.isEmpty())
        tree.forEach { assertNoCycles(it, mutableSetOf()) }
    }

    @Test
    fun testTreeBuilderMultipleIds() {
        val docs = persistDocs(doc(listOf("parentId", "alternativeParentId"), SourceType.JIRA))
        val views = documentTreeBuilder.buildTree(BV_USER, docs, documentStorage)
        Assert.assertEquals(1, views.size)
    }

    @Test
    fun nodesShouldOrderedByModifiedDate() {
        val now = System.currentTimeMillis()
        val DOC1_ID = "doc1"
        val DOC2_ID = "doc2"
        val docs = listOf(
                doc(listOf(DOC1_ID), SourceType.JIRA, updated = OffsetDateTime.now()),
                        doc(listOf(DOC2_ID), SourceType.JIRA, updated = OffsetDateTime.now().minusSeconds(10000)))
        val othersGroup1 = documentTreeBuilder.buildTree(BV_USER, docs, documentStorage)[0]
        val jiraGroup1 = othersGroup1.subNodes.first()
        assertTrue(jiraGroup1.subNodes.first().doc.ids.contains(DOC1_ID))

        val othersGroup2 = documentTreeBuilder.buildTree(BV_USER, docs.reversed(), documentStorage)[0]
        val jiraGroup2 = othersGroup2.subNodes.first()
        assertTrue(jiraGroup2.subNodes.first().doc.ids.contains(DOC1_ID))
    }

    private fun doc(ids: List<String>, sourceType: SourceType, refIds: List<BVDocumentRef> = listOf(), updated:OffsetDateTime = dateSeq()): BVDocument =
            BVDocument(
                    ids = ids.map { BVDocumentId(it) }.toSet(),
                    title = ids.first(),
                    key = "key",
                    body = "body",
                    updated = updated,
                    created = updated,
                    closed = updated,
                    httpUrl = "httpUrl",
                    refs = refIds,
                    status = BVDocumentStatus.BACKLOG,
                    sourceName = "sourceName")

    private fun dateSeq(): OffsetDateTime {
        TIME_INSTANT = TIME_INSTANT.plusSeconds(10)
        return TIME_INSTANT
    }
    private fun docRef(ref: String, type: RelativeHierarchyType = RelativeHierarchyType.UNSPECIFIED) =
            BVDocumentRef(BVDocumentId(ref), type)

    private fun assertNoCycles(node: BVDocumentViewTreeNode, visited:MutableSet<BVDocumentViewTreeNode>) {
        if (visited.contains(node)) {
            throw AssertionError("Cycle detected")
        }
        visited.add(node)
        node.subNodes.forEach {
            assertNoCycles(it, visited);
        }
        visited.remove(node)
    }

    fun persistDocs(vararg docs:BVDocument): List<BVDocument> {
        docs.forEach { doc->
            documentStorage.updateDocument(doc, BV_USER)
        }
        return docs.toList()
    }
}