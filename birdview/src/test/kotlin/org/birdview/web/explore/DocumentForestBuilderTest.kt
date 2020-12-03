package org.birdview.web.explore

import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.model.BVDocumentRef
import org.birdview.model.BVDocumentStatus
import org.birdview.model.RelativeHierarchyPosition
import org.birdview.model.ReportType
import org.birdview.source.SourceType
import org.birdview.storage.memory.BVDocumentPredicate
import org.birdview.storage.memory.BVInMemoryDocumentStorage
import org.birdview.web.explore.model.BVDocumentViewTreeNode
import org.junit.Assert
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.*

//@RunWith(MockitoJUnitRunner::class)
class DocumentForestBuilderTest {
    private var TIME_INSTANT = System.currentTimeMillis()

    private val documentStorage = BVInMemoryDocumentStorage(mock(BVDocumentPredicate::class.java))

    @Test
    fun testSimpleTreeBuilt() {
        val PARENT_ID = "parentId"
        val CHILDREN_ID = "childrenId"
        val GRANDCHILDREN_ID = "grandChildrenId"
        val docs = persistDocs(
                doc(listOf(PARENT_ID), SourceType.JIRA),
                doc(listOf(CHILDREN_ID), SourceType.JIRA, listOf(docRef(PARENT_ID, RelativeHierarchyPosition.LINK_TO_PARENT))),
                doc(listOf(GRANDCHILDREN_ID), SourceType.GDRIVE, listOf(docRef(CHILDREN_ID, RelativeHierarchyPosition.LINK_TO_PARENT)))
        )
        val views = DocumentTreeBuilder.buildTree(docs, documentStorage, ReportType.WORKED)
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
        val tree = DocumentTreeBuilder.buildTree(docs, documentStorage, ReportType.WORKED)

        assertFalse(tree.isEmpty())
        assertFalse(tree.first().alternativeDocs.any() { it.ids.contains("id3") })
        tree.forEach { assertNoCycles(it, mutableSetOf()) }
    }

    @Test
    fun testCyclicDependencie2DoNotCreateCycleInTree() {
        val docs = persistDocs(
                doc(listOf("id0"), SourceType.JIRA, listOf(docRef("id1", RelativeHierarchyPosition.LINK_TO_CHILD))),
                doc(listOf("id1"), SourceType.JIRA, listOf(docRef("id2", RelativeHierarchyPosition.LINK_TO_CHILD), docRef("id3", RelativeHierarchyPosition.LINK_TO_CHILD))),
                doc(listOf("id2"), SourceType.JIRA, listOf(docRef("id1", RelativeHierarchyPosition.LINK_TO_CHILD))),
                doc(listOf("id3"), SourceType.JIRA, listOf(docRef("id1", RelativeHierarchyPosition.LINK_TO_CHILD), docRef("id2", RelativeHierarchyPosition.LINK_TO_CHILD)))
        )
        val tree = DocumentTreeBuilder.buildTree(docs, documentStorage, ReportType.WORKED)

        assertFalse(tree.isEmpty())
        assertTrue(tree.first().doc.ids.contains("id0"))
        assertFalse(tree.first().alternativeDocs.any() { it.ids.contains("id3") })
        tree.forEach { assertNoCycles(it, mutableSetOf()) }
    }

    @Test
    fun testDirectedCyclicDependenciesDoNotCreateCycleInTree() {
        val docs = persistDocs(
                doc(listOf("id1", "id1Alt"), SourceType.JIRA, listOf(docRef("id2", RelativeHierarchyPosition.LINK_TO_CHILD))),
                doc(listOf("id2"), SourceType.JIRA, listOf(docRef("id3", RelativeHierarchyPosition.LINK_TO_CHILD))),
                doc(listOf("id3"), SourceType.JIRA, listOf(docRef("id1Alt", RelativeHierarchyPosition.LINK_TO_CHILD)))
        )
        val tree = DocumentTreeBuilder.buildTree(docs, documentStorage, ReportType.WORKED)

        assertFalse(tree.isEmpty())
        tree.forEach { assertNoCycles(it, mutableSetOf()) }
    }

    @Test
    fun testDirectedBackwardDependency() {
        val docs = persistDocs(
                doc(listOf("id1", "id1Alt"), SourceType.JIRA, listOf(docRef("id2", RelativeHierarchyPosition.LINK_TO_CHILD))),
                doc(listOf("id2"), SourceType.JIRA, listOf()),
                doc(listOf("id3"), SourceType.JIRA, listOf(docRef("id1Alt", RelativeHierarchyPosition.LINK_TO_CHILD)))
        )
        val tree = DocumentTreeBuilder.buildTree(docs, documentStorage, ReportType.WORKED)

        assertFalse(tree.isEmpty())
        tree.forEach { assertNoCycles(it, mutableSetOf()) }
    }

    @Test
    fun testTreeBuilderMultipleIds() {
        val docs = persistDocs(doc(listOf("parentId", "alternativeParentId"), SourceType.JIRA))
        val views = DocumentTreeBuilder.buildTree(docs, documentStorage, ReportType.WORKED)
        Assert.assertEquals(1, views.size)
    }

    @Test
    fun nodesShoyldOrderedByModifiedDate() {
        val now = System.currentTimeMillis()
        val DOC1_ID = "doc1"
        val DOC2_ID = "doc2"
        val docs = listOf(
                doc(listOf(DOC1_ID), SourceType.JIRA, updated = Date(now)),
                        doc(listOf(DOC2_ID), SourceType.JIRA, updated = Date(now-10000)))
        val views1 = DocumentTreeBuilder.buildTree(docs, documentStorage, ReportType.WORKED)
        Assert.assertTrue(views1[0].doc.ids.contains(DOC1_ID))

        val views2 = DocumentTreeBuilder.buildTree(docs.reversed(), documentStorage, ReportType.WORKED)
        Assert.assertTrue(views1[0].doc.ids.contains(DOC1_ID))
    }

    private fun doc(ids: List<String>, sourceType: SourceType, refIds: List<BVDocumentRef> = listOf(), updated:Date = dateSeq()): BVDocument =
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
                    sourceType = sourceType,
                    sourceName = "sourceName")

    private fun dateSeq():Date {
        TIME_INSTANT += 10
        return Date(TIME_INSTANT);
    }
    private fun docRef(ref: String, type: RelativeHierarchyPosition = RelativeHierarchyPosition.UNSPECIFIED) =
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
            documentStorage.updateDocument(doc)
        }
        return docs.toList()
    }
}