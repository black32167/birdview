package org.birdview.web.explore

import org.birdview.analysis.BVDocument
import org.birdview.source.SourceType
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
            val refsIds = doc.refsIds// + doc.groupIds.map { it.id }
            refsIds.forEach { refId->
                id2Docs[refId]
                        ?.also { referncedDoc ->
                            val parentNode:BVDocumentViewTreeNode?
                            val childNode:BVDocumentViewTreeNode?
                            if (getPriority(referncedDoc.sourceType) <= getPriority(doc.sourceType)) {
                                parentNode = id2Nodes[refId]
                                childNode = id2Nodes[doc.ids.first().id]
                            } else {
                                parentNode = id2Nodes[doc.ids.first().id]
                                childNode = id2Nodes[refId]
                            }
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

        return rootNodes.toList().sortedByDescending { it.lastUpdated }
    }

    private fun getPriority(sourceType: SourceType): Int = when(sourceType) {
        SourceType.JIRA, SourceType.TRELLO -> 1
        SourceType.SLACK -> 2
        SourceType.GDRIVE -> 3
        SourceType.GITHUB -> 4
    }
}