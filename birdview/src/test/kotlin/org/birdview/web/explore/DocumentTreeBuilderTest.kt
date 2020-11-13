package org.birdview.web.explore

import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.model.BVDocumentRef
import org.birdview.model.BVDocumentStatus
import org.birdview.source.SourceType
import org.birdview.web.explore.model.BVDocumentViewTreeNode
import org.junit.Assert
import org.junit.Test
import java.lang.AssertionError
import java.util.*

class DocumentTreeBuilderTest {
    @Test
    fun testSimpleTreeBuilt() {
        val PARENT_ID = "parentId"
        val CHILDREN_ID = "childrenId"
        val GRANDCHILDREN_ID = "grandChildrenId"
        val docs = listOf(
                doc(listOf(PARENT_ID), SourceType.JIRA),
                doc(listOf(CHILDREN_ID), SourceType.JIRA, listOf(docRef(PARENT_ID))),
                doc(listOf(GRANDCHILDREN_ID), SourceType.GDRIVE, listOf(docRef(CHILDREN_ID)))
        )
        val views = DocumentTreeBuilder.buildTree(docs)
        Assert.assertEquals(1, views.size)
        Assert.assertEquals(1, views.first().subNodes.size)
        Assert.assertEquals(1, views.first().subNodes[0].subNodes.size)
        Assert.assertEquals(0, views.first().subNodes[0].subNodes[0].subNodes.size)
    }

    @Test
    fun testCycleDetection() {
        val docs = listOf(
                doc(listOf("id1", "id1Alt"), SourceType.JIRA, listOf(docRef("id2"))),
                doc(listOf("id2"), SourceType.JIRA, listOf(docRef("id3"))),
                doc(listOf("id3"), SourceType.JIRA, listOf(docRef("id1Alt")))
        )
        val tree = DocumentTreeBuilder.buildTree(docs)

        tree.forEach { assertNoCycles(it, mutableSetOf()) }
    }

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

    @Test
    fun testTreeBuilderMultipleIds() {
        val docs = listOf(doc(listOf("parentId", "alternativeParentId"), SourceType.JIRA))
        val views = DocumentTreeBuilder.buildTree(docs)
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
        val views1 = DocumentTreeBuilder.buildTree(docs)
        Assert.assertTrue(views1[0].doc.ids.contains(DOC1_ID))

        val views2 = DocumentTreeBuilder.buildTree(docs.reversed())
        Assert.assertTrue(views1[0].doc.ids.contains(DOC1_ID))
    }

    private fun doc(ids: List<String>, sourceType: SourceType, refIds: List<BVDocumentRef> = listOf(), updated:Date = Date()): BVDocument =
            BVDocument(
                    ids = ids.map { BVDocumentId(it, "type", "sourceName") }.toSet(),
                    title = ids.first(),
                    key = "key",
                    body = "body",
                    updated = updated,
                    created = updated,
                    closed = updated,
                    httpUrl = "httpUrl",
                    status = BVDocumentStatus.BACKLOG,
                    sourceType = sourceType,
                    refs = refIds
            )

    private fun docRef(ref: String) = BVDocumentRef(ref, sourceName = "sourceName")
}