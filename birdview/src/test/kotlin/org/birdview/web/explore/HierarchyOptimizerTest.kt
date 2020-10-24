package org.birdview.web.explore

import org.birdview.analysis.Priority
import org.birdview.web.explore.model.BVDocumentView
import org.birdview.web.explore.model.BVDocumentViewTreeNode
import org.junit.Assert
import org.junit.Test
import java.util.*

class HierarchyOptimizerTest {
    @Test
    fun redundantNodesShouldBeOptimized() {
        val root = node("parentId", listOf(
                        node("childrenId", listOf(
                                node("grandChildrenId")))))

        HierarchyOptimizer.optimizeHierarchy(root)

        Assert.assertEquals(1, root.subNodes.size)
        Assert.assertEquals(0, root.subNodes[0].subNodes.size)
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
                        id = id,
                        lastUpdater = "lastUpdater",
                        sourceName = "sourceName",
                        priority = Priority.NORMAL
                    ),
                    subNodes.toMutableList(),
                    lastUpdated = Date()
            )
}