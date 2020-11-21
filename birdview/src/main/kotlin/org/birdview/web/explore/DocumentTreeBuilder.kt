package org.birdview.web.explore

import org.birdview.analysis.BVDocument
import org.birdview.model.BVDocumentRelation
import org.birdview.source.BVDocumentsRelation
import org.birdview.web.explore.model.BVDocumentViewTreeNode
object DocumentTreeBuilder {
    fun buildTree(_docs: List<BVDocument>): List<BVDocumentViewTreeNode> {
        // Map all documents
        val id2Docs =  mutableMapOf<String, BVDocument>()
        _docs.forEach { doc ->
           doc.ids.forEach { docId->
               id2Docs[docId.id] = doc
           }
        }

        // Create views
        val id2Nodes = mutableMapOf<String, BVDocumentViewTreeNode>()
        val rootNodes = mutableSetOf<BVDocumentViewTreeNode>()
        _docs.forEach { doc ->
            val node = BVDocumentViewTreeNode(doc = BVDocumentViewFactory.create(doc), lastUpdated = doc.updated)
            doc.ids.forEach { docId->
                id2Nodes[docId.id] = node
                rootNodes += node
            }
        }

        // Link documents
        _docs.forEach { doc ->
            val refsIds:List<BVDocumentRelation> = doc.relations// + doc.groupIds.map { it.id }
            refsIds.forEach { ref->
                val referncedDoc = id2Docs[ref.ref]
                if (referncedDoc != null) {
                    val relation = BVDocumentsRelation.from(referncedDoc, doc, ref.refType)
                    if (relation != null) {
                        val parentNode = id2Nodes[relation.parent.ids.first().id]
                        val childNode = id2Nodes[relation.child.ids.first().id]

                        if (parentNode != null && childNode != null) {
                            parentNode.subNodes.add(childNode)
                            val parentNodeLastUpdated = parentNode.lastUpdated
                            if (parentNodeLastUpdated == null ||
                                    parentNodeLastUpdated.before(childNode.lastUpdated)) {
                                parentNode.lastUpdated = childNode.lastUpdated
                            }
                            rootNodes.remove(childNode)
                        }
                    }
                    }
                }
            }

        return rootNodes.toList().sortedByDescending { it.lastUpdated }
    }
}