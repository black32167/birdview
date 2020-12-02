package org.birdview.web.explore

import org.birdview.analysis.BVDocument
import org.birdview.model.RelativeHierarchyPosition
import org.birdview.model.ReportType
import org.birdview.source.BVDocumentsRelation
import org.birdview.storage.BVDocumentStorage
import org.birdview.web.explore.model.BVDocumentViewTreeNode
import java.util.Comparator

object DocumentTreeBuilder {
    class DocumentForest (
            private val documentStorage: BVDocumentStorage,
            private val subNodesComparator: Comparator<BVDocumentViewTreeNode>
    ) {
        private val internalId2Node = mutableMapOf<String, BVDocumentViewTreeNode>()

        fun addAndGetDocNode(doc: BVDocument): BVDocumentViewTreeNode {
            val existingNode = internalId2Node[doc.internalId]
            if (existingNode != null) {
                return existingNode;
            }

            val docNode = BVDocumentViewTreeNode(
                    doc = BVDocumentViewFactory.create(doc),
                    lastUpdated = doc.updated,
                    sourceType = doc.sourceType,
                    subNodesComparator = subNodesComparator
            )
            internalId2Node[doc.internalId] = docNode
            doc.refs.forEach { ref ->
                val referencedDocNode: BVDocumentViewTreeNode? = documentStorage.getDocuments(setOf(ref.docId.id))
                        .firstOrNull()
                        ?.let { addAndGetDocNode(it) }

                if (referencedDocNode != null) {
                    val relation = BVDocumentsRelation.from(referencedDocNode, docNode, ref.hierarchyPosition)

                    // Hierarchical relation
                    if (relation != null) {
                        relation.apply {
                            parent.addSubNode(child)
                            val parentNodeLastUpdated = parent.lastUpdated
                            if (parentNodeLastUpdated == null ||
                                    parentNodeLastUpdated.before(child.lastUpdated)) {
                                parent.lastUpdated = child.lastUpdated
                            }
                        }
                    } else {
                        // Alternatives
                        docNode.addAlternative(referencedDocNode)
                        if (internalId2Node.containsKey(docNode.internalId)) {
                            internalId2Node.remove(referencedDocNode.doc.internalId)
                        }
                    }
                }
            }

            return docNode
        }

        fun collapseCycles() {
            while (!internalId2Node.isEmpty() &&
                    collapseCycles(
                            internalId2Node.values.first(), mutableSetOf(), mutableSetOf())) {
            }
        }

        private fun collapseCycles(node: BVDocumentViewTreeNode, visiting: MutableSet<BVDocumentViewTreeNode>, visitedIds: MutableSet<String>):Boolean {
            if (visitedIds.contains(node.internalId)) {
                return false
            }
            if (visiting.contains(node)) {
                // cycle
                visiting.filter { it != node }.forEach { cycleNode->
                    if (internalId2Node.containsKey(cycleNode.internalId)) {
                        node.addAlternative(cycleNode)
                        internalId2Node.remove(cycleNode.internalId)
                    }
                }
                return true
            }

            visiting += node
            for (subNode in node.subNodes) {
                if (collapseCycles(subNode, visiting, visitedIds)) {
                    return true
                }
            }
            visiting -= node
            visitedIds += node.internalId
            return false
        }

        fun getRoots(): List<BVDocumentViewTreeNode> = internalId2Node.values
                .filter (BVDocumentViewTreeNode::isRoot)
                .toList()
                .sortedByDescending { it.lastUpdated }

    }

    fun buildTree(_docs: List<BVDocument>, documentStorage: BVDocumentStorage, reportType: ReportType): List<BVDocumentViewTreeNode> {
        val comparator: Comparator<BVDocumentViewTreeNode> = if (reportType == ReportType.WORKED) {
            compareByDescending<BVDocumentViewTreeNode> { it.lastUpdated }
                    .thenByDescending { it.doc.priority.ordinal }
        } else {
            compareByDescending<BVDocumentViewTreeNode>{ it.doc.priority.ordinal }
                    .thenByDescending { it.lastUpdated }
        }
        val tree = DocumentForest(documentStorage, comparator)

        _docs.forEach { doc ->
            tree.addAndGetDocNode(doc)
        }

        tree.collapseCycles()

        return tree.getRoots()
    }

}