package org.birdview.web.explore

import org.birdview.analysis.Priority
import org.birdview.source.SourceType
import org.birdview.web.explore.model.BVDocumentView
import org.birdview.web.explore.model.BVDocumentViewTreeNode
import org.junit.Assert
import org.junit.Test
import java.time.OffsetDateTime

class HierarchyOptimizerTest {
    @Test
    fun redundantNodesShouldBeOptimized() {
        val root = node("parentId", listOf(
                        node("childrenId", listOf(
                                node("grandChildrenId")))))

        HierarchyOptimizer.optimizeHierarchy(root)

        Assert.assertEquals(1, root.subNodes.size)
        Assert.assertEquals(0, root.subNodes.first().subNodes.size)
        Assert.assertEquals("grandChildrenId", root.subNodes.first().internalId)
     }

    @Test
    fun redundantNodesShouldBeOptimizedInLargerHierarchy() {
        val root = node("grandParentId", listOf(
                node("parentId", listOf(
                        node("childrenId1", listOf(
                                node("grandChildrenId"))),
                        node("childrenId2", listOf(
                                node("grandChildrenId")))
                ))
        ))

        HierarchyOptimizer.optimizeHierarchy(root)

        Assert.assertEquals(2, root.subNodes.size)
        Assert.assertEquals(0, root.subNodes.first().subNodes.size)
        Assert.assertEquals("grandChildrenId", root.subNodes.first().internalId)
    }

    @Test
    fun richNodesShouldNotBeOptimized() {
        val root = node("parentId", listOf(
                node("childrenId", listOf(
                        node("grandChildrenId1"),
                        node("grandChildrenId2")
                ))))

        HierarchyOptimizer.optimizeHierarchy(root)

        Assert.assertEquals(2, root.subNodes.size)
    }

    @Test
    fun oneLevelHoerarchyShouldNotBeOptimized() {
        val root = node("parentId", listOf(
                node("childrenId")))

        HierarchyOptimizer.optimizeHierarchy(root)

        Assert.assertEquals(1, root.subNodes.size)
    }

    private fun node(id: String, subNodes: List<BVDocumentViewTreeNode> = listOf()): BVDocumentViewTreeNode =
            BVDocumentViewTreeNode(
                    BVDocumentView(
                        ids = listOf(id),
                        title = id,
                        key = "key",
                        updated = "2020-12-15",
                        httpUrl = "httpUrl",
                        status = "BACKLOG",
                        internalId = id,
                        lastUpdater = "lastUpdater",
                        sourceName = "sourceName",
                        priority = Priority.NORMAL
                    ),
                    lastUpdated = OffsetDateTime.now(),
                    sourceType = SourceType.JIRA
            ).also {
                it.subNodes = subNodes.toMutableSet()
            }
}