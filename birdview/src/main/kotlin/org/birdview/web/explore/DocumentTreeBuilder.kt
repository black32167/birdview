package org.birdview.web.explore

import org.birdview.analysis.BVDocument
import org.birdview.model.BVDocumentRef
import org.birdview.source.BVDocumentNodesRelation
import org.birdview.storage.BVDocumentStorage
import org.birdview.web.explore.model.BVDocumentViewTreeNode
import org.slf4j.LoggerFactory
import javax.inject.Named

@Named
class DocumentTreeBuilder(
    val documentViewFactory: BVDocumentViewFactory
) {
    inner class DocumentForestBuilder (
            private val documentStorage: BVDocumentStorage
    ) {
        private val log = LoggerFactory.getLogger(DocumentTreeBuilder::class.java)
        private val internalId2Node = mutableMapOf<String, BVDocumentViewTreeNode>()
        private val alternatives = mutableMapOf<String, MutableSet<String>>()

        fun createNode(doc: BVDocument): BVDocumentViewTreeNode =
            internalId2Node.computeIfAbsent(doc.internalId) {
                BVDocumentViewTreeNode(
                    doc = documentViewFactory.create(doc),
                    lastUpdated = doc.updated,
                    sourceType = doc.sourceType
                )
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
                throw IllegalStateException("Could not retrieve all the cycle nodes (cycle nodes:${nodeIds}, found nodes:${nodes.map { it.internalId }})")
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

        fun addAndLinkNodes(_docs: List<BVDocument>) {
            log.info("Linking docs: ${_docs.map { "\n\t${it.title}(${it.ids.firstOrNull()?.id})" }.joinToString()}")

            val processedIds = mutableSetOf<String>()
            var docsToProcess:Map<String, BVDocument> = _docs.filter { !processedIds.contains(it.internalId) }.associateBy { it.internalId }

            while (docsToProcess.isNotEmpty()) {
                log.info("resolving refs for docs ({})", docsToProcess.size)
                val refDocs = mutableMapOf<String, BVDocument>()
                docsToProcess.values.forEach { originalDoc ->
                    processedIds += originalDoc.internalId
                    val originalDocNode = createNode(originalDoc)

                    val outgoingLinks: List<BVDocumentRef> = originalDoc.refs
                    val incomingLinks: List<BVDocumentRef> = originalDoc.ids.map { it.id }
                        .let { externalIds -> documentStorage.getIncomingRefsByExternalIds(externalIds.toSet()) }
                    log.info("Linking '{}':{}",
                        "${originalDoc.title}(${originalDoc.ids.firstOrNull()?.id})",
                        (incomingLinks.map {"\n\t${it.docId.id}->"} + outgoingLinks.map {"\n\t->${it.docId.id}"}).joinToString())

                    for (ref in (outgoingLinks + incomingLinks)) {
                        val referredDoc = documentStorage.getDocuments(setOf(ref.docId.id)).firstOrNull()
                        if (referredDoc != null) {

                            val relation =
                                BVDocumentNodesRelation.from(referredDoc, originalDoc, ref.hierarchyType)

                            if (relation != null /*&& relation.child.internalId == originalDocNode.internalId */) {
                                if (relation.child.internalId == originalDocNode.internalId) {
                                    val parentNode = createNode(relation.parent)
                                    val childNode = createNode(relation.child)
                                    refDocs[referredDoc.internalId] = referredDoc
                                    parentNode.addSubNode(childNode)
                                    val parentNodeLastUpdated = parentNode.lastUpdated
                                    if (parentNodeLastUpdated == null ||
                                        parentNodeLastUpdated.isBefore(childNode.lastUpdated)
                                    ) {
                                        parentNode.lastUpdated = childNode.lastUpdated
                                    }
                                }
                            } else {
                                alternatives.computeIfAbsent(originalDocNode.internalId) { mutableSetOf() } +=
                                    createNode(referredDoc).internalId
                            }
                        }
                    }
                }
                docsToProcess = refDocs.filter { (internalId, _) -> !processedIds.contains(internalId) }
            }
        }
    }

    fun buildTree(_docs: List<BVDocument>, documentStorage: BVDocumentStorage): List<BVDocumentViewTreeNode> {
        val tree = DocumentForestBuilder(documentStorage)

        tree.addAndLinkNodes(_docs)

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