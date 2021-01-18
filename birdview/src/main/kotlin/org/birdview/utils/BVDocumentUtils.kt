package org.birdview.utils

import org.birdview.analysis.BVDocument
import org.birdview.model.RelativeHierarchyType

object BVDocumentUtils {
    fun getReferencedDocIdsByHierarchyType(filteredDocs: Collection<BVDocument>, includeRelationTypes:Set<RelativeHierarchyType>): List<String> {
        val materializedIds = filteredDocs
            .flatMap { it.ids }
            .distinct()
            .map { it.id }
            .toSet()
        val referencedIds = (filteredDocs.flatMap {
            it.refs.asSequence()
                .filter { includeRelationTypes.contains(it.hierarchyType) }
                .map { it.docId.id }
                .filter { !materializedIds.contains(it) }
        }).distinct()
        return referencedIds
    }
}