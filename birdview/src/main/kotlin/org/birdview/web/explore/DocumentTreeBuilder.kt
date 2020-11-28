package org.birdview.web.explore

import org.birdview.analysis.BVDocument
import org.birdview.source.BVDocumentsRelation
import org.birdview.storage.BVDocumentStorage
import org.birdview.web.explore.model.BVDocumentViewTreeNode

object DocumentTreeBuilder {
    class DocumentForest(private val documentStorage: BVDocumentStorage) {
        private val internalId2Node = mutableMapOf<String, BVDocumentViewTreeNode>()
        private val rootNodes = mutableSetOf<BVDocumentViewTreeNode>()

        fun addAndGetDocNode(doc: BVDocument): BVDocumentViewTreeNode {
            val existingNode = internalId2Node[doc.internalId]
            if (existingNode != null) {
                return existingNode;
            }

            val docNode = BVDocumentViewTreeNode(
                    doc = BVDocumentViewFactory.create(doc),
                    lastUpdated = doc.updated,
                    sourceType = doc.sourceType)
            rootNodes.add(docNode)
            internalId2Node[doc.internalId] = docNode

            doc.refs.forEach { ref ->
                val referencedDocNode: BVDocumentViewTreeNode? = documentStorage.getDocuments(setOf(ref.docId.id))
                        .firstOrNull()
                        ?.let { addAndGetDocNode(it) }

                if (referencedDocNode != null) {
                    val relation = BVDocumentsRelation.from(referencedDocNode, docNode, ref.hierarchyPosition)

                    // Hierarchical relation
                    if (relation != null && !subtreeContains(relation.child, relation.parent)) {
                        relation.apply {
                            parent.addSubNode(child)
                            val parentNodeLastUpdated = parent.lastUpdated
                            if (parentNodeLastUpdated == null ||
                                    parentNodeLastUpdated.before(child.lastUpdated)) {
                                parent.lastUpdated = child.lastUpdated
                            }
                            rootNodes.remove(child)
                        }
                    } else {
                        // Alternatives
                        if (docNode.addAlternative(referencedDocNode)) {
                            rootNodes.remove(referencedDocNode)
                        }
                        // TODO: collapse cycle?
                    }
                }
            }

            return docNode
        }

        fun getRoots(): List<BVDocumentViewTreeNode> = rootNodes.toList().sortedByDescending { it.lastUpdated }

        private fun subtreeContains(parentNode: BVDocumentViewTreeNode, targetNode: BVDocumentViewTreeNode): Boolean {
            if (parentNode == targetNode) {
                return true
            }
            for (children in parentNode.subNodes) {
                if (subtreeContains(children, targetNode)) {
                    return true
                }
            }
            return false
        }
    }

    fun buildTree(_docs: List<BVDocument>, documentStorage: BVDocumentStorage): List<BVDocumentViewTreeNode> {
        // Create views

        val tree = DocumentForest(documentStorage)

        _docs.forEach { doc ->
            tree.addAndGetDocNode(doc)
        }

        return tree.getRoots()
    }

}