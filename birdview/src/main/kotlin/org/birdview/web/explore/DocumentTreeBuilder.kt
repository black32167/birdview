package org.birdview.web.explore

import org.birdview.analysis.BVDocument
import org.birdview.model.BVDocumentRef
import org.birdview.model.RelativeHierarchyType
import org.birdview.source.BVDocumentRelationFactory
import org.birdview.source.SourceType
import org.birdview.storage.BVDocumentStorage
import org.birdview.storage.BVUserSourceConfigStorage
import org.birdview.web.explore.model.BVDocumentView
import org.birdview.web.explore.model.BVDocumentViewTreeNode
import org.slf4j.LoggerFactory
import javax.inject.Named

@Named
class DocumentTreeBuilder(
    val documentViewFactory: BVDocumentViewFactory,
    val documentRelationFactory: BVDocumentRelationFactory,
    val userSourceConfigStorage: BVUserSourceConfigStorage
) {
    inner class DocumentForestBuilder (
            private val documentStorage: BVDocumentStorage
    ) {
        private val log = LoggerFactory.getLogger(DocumentTreeBuilder::class.java)
        private val internalId2Node = mutableMapOf<String, BVDocumentViewTreeNode>()
        private val alternatives = mutableMapOf<String, MutableSet<String>>()

        fun createNode(bvUser:String, doc: BVDocument): BVDocumentViewTreeNode =
            internalId2Node.computeIfAbsent(doc.internalId) {
                BVDocumentViewTreeNode(
                    doc = documentViewFactory.create(doc),
                    lastUpdated = doc.updated,
                    sourceType = userSourceConfigStorage.getSource(bvUser = bvUser, sourceName = doc.sourceName)!!.sourceType
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

        fun addAndLinkNodes(bvUser: String, inputDocs: List<BVDocument>) {
            log.info("Linking docs: ${inputDocs.map { "\n\t${it.title}(${it.ids.firstOrNull()?.id})" }.joinToString()}")

            val referringDocs = inputDocs
                .flatMap { it.ids }
                .map { it.id }
                .let { allExternalIds -> documentStorage.getReferringDocuments(allExternalIds.toSet()) }
            val targetDocId2ReferringDocs: Map<String, List<BVDocument>> = referringDocs
                .flatMap { referringDoc: BVDocument ->  referringDoc.refs.map { it.docId.id to referringDoc } }
                .groupBy({it.first}, {it.second})

            var id2doc:Map<String, BVDocument> = inputDocs.associateBy { it.internalId }
            val externalId2Doc: Map<String, BVDocument> = (inputDocs + referringDocs)
                .flatMap { doc-> doc.ids.map { it.id to doc } }
                .associateBy ({ it.first }, { it.second })

            val processedIds = mutableSetOf<String>()
            while (id2doc.isNotEmpty()) {
                log.info("resolving refs for docs ({})", id2doc.size)
                val refDocs = mutableMapOf<String, BVDocument>()
                id2doc.values.forEach { originalDoc ->
                    processedIds += originalDoc.internalId
                    val originalDocNode = createNode(bvUser = bvUser, originalDoc)

                    val outgoingLinks: List<BVDocumentRef> = originalDoc.refs
                    val externalIds = originalDoc.ids.map { it.id }
                    val incomingLinks: List<BVDocumentRef> = externalIds
                        .flatMap { externalId-> targetDocId2ReferringDocs.get(externalId) ?: listOf() }
                        .flatMap { parentCandidateDoc ->
                            parentCandidateDoc.refs.filter { ref ->
                                externalIds.contains(ref.docId.id)
                            }.mapNotNull { originalRef->parentCandidateDoc.ids.firstOrNull()?.let { parentId->
                                val newRelType = if (originalRef.hierarchyType == RelativeHierarchyType.LINK_TO_PARENT) {
                                    RelativeHierarchyType.LINK_TO_CHILD
                                } else if (originalRef.hierarchyType == RelativeHierarchyType.LINK_TO_CHILD) {
                                    RelativeHierarchyType.LINK_TO_PARENT
                                } else {
                                    RelativeHierarchyType.UNSPECIFIED
                                }
                                BVDocumentRef(parentId, newRelType)
                            }}
                        }

                    log.info("Linking '{}':{}",
                        "${originalDoc.title}(${originalDoc.ids.firstOrNull()?.id})",
                        (incomingLinks.map {"\n\t${it.docId.id}->"} + outgoingLinks.map {"\n\t->${it.docId.id}"}).joinToString())

                    for (ref in (outgoingLinks + incomingLinks)) {
                        externalId2Doc[ref.docId.id]?.also {  referredDoc ->
                            val relation =
                                documentRelationFactory.from(bvUser = bvUser, referredDoc, originalDoc, ref.hierarchyType)

                            if (relation != null /*&& relation.child.internalId == originalDocNode.internalId */) {
                                if (relation.child.internalId == originalDocNode.internalId) {
                                    val parentNode = createNode(bvUser = bvUser, relation.parent)
                                    val childNode = createNode(bvUser = bvUser, relation.child)
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
                                //TODO: For now, commenting alternative representation resolution
                              /*  alternatives.computeIfAbsent(originalDocNode.internalId) { mutableSetOf() } +=
                                    createNode(referredDoc).internalId*/
                            }
                        }
                    }
                }
                id2doc = refDocs.filter { (internalId, _) -> !processedIds.contains(internalId) }
            }
        }
    }

    fun buildTree(bvUser: String, _docs: List<BVDocument>, documentStorage: BVDocumentStorage): List<BVDocumentViewTreeNode> {
        val tree = DocumentForestBuilder(documentStorage)

        tree.addAndLinkNodes(bvUser = bvUser, _docs)

        try {
            tree.mergeAlternatives()
            val cycles = tree.findCycles()
            cycles.forEach {
                tree.collapseNodes(it)
            }

            // Group ungrouped by types
            val roots = tree.getRoots();
            val (singles, trees) = roots.partition { it.subNodes.isEmpty() }

            if (singles.isEmpty()) {
                return trees
            } else {
                val groupedSingles = singles.groupBy { it.sourceType }
                    .map { (sourceType, nodes) ->
                        BVDocumentViewTreeNode(
                            doc = BVDocumentView.grouping(sourceType.name, sourceType.name.toLowerCase().capitalize()),
                            sourceType = SourceType.NONE
                        ).also { groupingNode -> nodes.forEach { groupingNode.addSubNode(it) } }
                    }


                val otherDocNode = BVDocumentViewTreeNode(
                    doc = BVDocumentView.grouping("other_docs", "Other..."),
                    sourceType = SourceType.NONE
                ).also { groupingNode -> groupedSingles.forEach { groupingNode.addSubNode(it) } }

                return trees + otherDocNode
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}