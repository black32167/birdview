package org.birdview.web.explore

import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.model.BVDocumentStatus
import org.birdview.source.SourceType
import org.junit.Assert
import org.junit.Test
import java.util.*

class DocumentTreeBuilderTest {
    @Test
    fun testTreeBuilder() {
        val PARENT_ID = "parentId"
        val CHILDREN_ID = "childrenId"
        val GRANDCHILDREN_ID = "grandChildrenId"
        val docs = listOf(
                doc(listOf(PARENT_ID), SourceType.JIRA),
                doc(listOf(CHILDREN_ID), SourceType.JIRA, setOf(PARENT_ID)),
                doc(listOf(GRANDCHILDREN_ID), SourceType.GDRIVE, setOf(CHILDREN_ID))
        )
        val views = DocumentTreeBuilder.buildTree(docs)
        Assert.assertEquals(1, views.size)
        Assert.assertEquals(1, views.first().subNodes.size)
        Assert.assertEquals(1, views.first().subNodes[0].subNodes.size)
        Assert.assertEquals(0, views.first().subNodes[0].subNodes[0].subNodes.size)
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

    private fun doc(ids: List<String>, sourceType: SourceType, refIds: Set<String> = setOf(), updated:Date = Date()): BVDocument =
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
                    refsIds = refIds
            )
}