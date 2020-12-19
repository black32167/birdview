package org.birdview.utils

import org.birdview.analysis.BVDocument
import org.birdview.model.RelativeHierarchyType

object BVDocumentUtils {
    fun getReferencedDocIdsByHierarchyType(filteredDocs: Collection<BVDocument>, includeRelationTypes:Set<RelativeHierarchyType>): Set<String> {
        val materializedIds = filteredDocs.flatMap { it.ids }.map { it.id }.toSet()
        val referencedIds = (filteredDocs.flatMap {
            it.refs
                .filter { includeRelationTypes.contains(it.hierarchyType) }
                .map{ it.docId.id }
        }).toSet()
        return referencedIds - materializedIds
    }
}