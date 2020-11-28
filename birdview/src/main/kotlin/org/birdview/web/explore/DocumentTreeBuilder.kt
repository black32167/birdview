package org.birdview.web.explore

import org.birdview.analysis.BVDocument
import org.birdview.source.BVDocumentsRelation
import org.birdview.storage.BVDocumentStorage
import org.birdview.web.explore.model.BVDocumentViewTreeNode

object DocumentTreeBuilder {
    class DocumentForest(private val documentStorage: BVDocumentStorage) {
        private val internalId2Node = mutableMapOf<String, BVDocumentViewTreeNode>()
        private val rootNodes = mutableSetOf<BVDocumentViewTreeNode>()

        fun addAndGetDocNode(doc: BVDocument): BVDocumentViewTreeNode =
            internalId2Node[doc.internalId]
                    ?: run {
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
                                relation?.apply {
                                    // Hierarchical relation
                                    if (!subtreeContains(child, parent)) {
                                        parent.addSubNode(child)
                                        val parentNodeLastUpdated = parent.lastUpdated
                                        if (parentNodeLastUpdated == null ||
                                                parentNodeLastUpdated.before(child.lastUpdated)) {
                                            parent.lastUpdated = child.lastUpdated
                                        }

                                        rootNodes.remove(child)
                                    } else {
                                        // Alternatives
                                        parent.alternativeNodes += child
                                        // TODO: collapse cycle?
                                    }
                                } ?: apply {
                                    // Alternatives
                                    docNode.alternativeNodes += referencedDocNode
                                }
                            }

                        }
                        docNode
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