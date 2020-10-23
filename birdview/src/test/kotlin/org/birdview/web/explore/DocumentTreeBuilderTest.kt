package org.birdview.web.explore

import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.analysis.BVDocumentOperation
import org.birdview.analysis.BVDocumentUser
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
                doc(PARENT_ID, SourceType.JIRA),
                doc(CHILDREN_ID, SourceType.JIRA, setOf(PARENT_ID)),
                doc(GRANDCHILDREN_ID, SourceType.GDRIVE, setOf(CHILDREN_ID))
        )
        val views = DocumentTreeBuilder.buildTree(docs)
        Assert.assertEquals(1, views.size)
        Assert.assertEquals(1, views[0].subNodes.size)
        Assert.assertEquals(1, views[0].subNodes[0].subNodes.size)
        Assert.assertEquals(0, views[0].subNodes[0].subNodes[0].subNodes.size)
    }

    private fun doc(id: String, sourceType: SourceType, refIds: Set<String> = setOf()): BVDocument =
            BVDocument(
                    ids = setOf(BVDocumentId(id, "type", "sourceName")),
                    title = id,
                    key = "key",
                    body = "body",
                    updated = Date(),
                    created = Date(),
                    closed = Date(),
                    httpUrl = "httpUrl",
                    status = BVDocumentStatus.BACKLOG,
                    sourceType = sourceType,
                    refsIds = refIds
            )
}