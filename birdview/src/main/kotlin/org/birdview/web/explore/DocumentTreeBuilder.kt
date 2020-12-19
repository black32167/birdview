package org.birdview.web.explore

import org.birdview.analysis.BVDocument
import org.birdview.source.BVDocumentsRelation
import org.birdview.storage.BVDocumentStorage
import org.birdview.web.explore.model.BVDocumentViewTreeNode
import java.lang.IllegalStateException
import java.util.*

object DocumentTreeBuilder {
    class DocumentForest (
            private val documentStorage: BVDocumentStorage,
            private val subNodesComparator: Comparator<BVDocumentViewTreeNode>?
    ) {
        private val internalId2Node = mutableMapOf<String, BVDocumentViewTreeNode>()
        private val alternatives = mutableMapOf<String, MutableSet<String>>()

        fun addAndGetDocNode(doc: BVDocument, depth: Int): BVDocumentViewTreeNode {
            if(depth > 100) {
                throw IllegalStateException("Depth is too high, probably caught up in endless recursion")
            }
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
                        ?.let { addAndGetDocNode(it, depth+1) }

                if (referencedDocNode != null) {
                    val relation = BVDocumentsRelation.from(referencedDocNode, docNode, ref.hierarchyType)

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
                        alternatives.computeIfAbsent(docNode.internalId) { mutableSetOf() } += referencedDocNode.internalId
                    }
                }
            }

            return docNode
        }

        fun mergeAlternatives() {
            alternatives.forEach { (docId, alternateIds) ->
                alternateIds.mapNotNull(internalId2Node::get).forEach { alternativeNode->
                    internalId2Node[docId]?.also { anchorNode->
                        anchorNode.mergeAlternative(alternativeNode)
                        internalId2Node.remove(alternativeNode.internalId)
                    }
                }
            }
        }

        fun findCycles(): Collection<Collection<String>> {
            val cyclesMap = mutableMapOf<String, MutableSet<String>>()
            internalId2Node.values.toList().forEach { node->
                        findCycles(node, mutableListOf(), mutableSetOf(), cyclesMap)
            }
            return cyclesMap.values.distinctBy { it }
        }

        fun collapseNodes(nodeIds: Collection<String>) {
            val nodes = nodeIds.mapNotNull (internalId2Node::get)
            if (nodes.size < nodeIds.size) {
                throw IllegalStateException("Could not retrieve all the cycle nodes")
            }
            val anchorNode = nodes.first()
            val tail = nodes.subList(1, nodes.size)
            tail.forEach { alternative->
                anchorNode.mergeAlternative(alternative)
                internalId2Node.remove(alternative.internalId)
            }
        }

        private fun findCycles(node: BVDocumentViewTreeNode, visiting: MutableList<BVDocumentViewTreeNode>, visitedIds: MutableSet<String>,
                               cycles: MutableMap<String, MutableSet<String>> // nodeId-><nodes in cycle>
        ) {
            if (visitedIds.contains(node.internalId)) {
                return
            }
            val cycleIdx = visiting.indexOf(node)
            if (cycleIdx != -1) {
                val foundCycle = visiting.subList(cycleIdx, visiting.size).map { it.internalId }
                val targetCycle = foundCycle.asSequence ().map (cycles::get).firstOrNull()
                    ?: mutableSetOf()
                targetCycle.addAll(foundCycle)
                foundCycle.forEach { cycles[it] = targetCycle }
                return
            }

            visiting += node
            for (subNode in node.subNodes) {
                findCycles(subNode, visiting, visitedIds, cycles)
            }
            visiting -= node
            visitedIds += node.internalId
        }

        fun getRoots(): List<BVDocumentViewTreeNode> = internalId2Node.values
                .filter (BVDocumentViewTreeNode::isRoot)
                .toList()
                .sortedByDescending { it.lastUpdated }
    }

    fun buildTree(_docs: List<BVDocument>, documentStorage: BVDocumentStorage, nodesComparator: Comparator<BVDocumentViewTreeNode>? = null): List<BVDocumentViewTreeNode> {
        val tree = DocumentForest(documentStorage, nodesComparator)

        _docs.forEach { doc ->
            tree.addAndGetDocNode(doc, 0)
        }

        try {
            tree.mergeAlternatives()
            val cycles = tree.findCycles()
            cycles.forEach {
                tree.collapseNodes(it)
            }

            return tree.getRoots()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}