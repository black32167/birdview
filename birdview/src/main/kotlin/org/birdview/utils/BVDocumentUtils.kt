package org.birdview.utils

import org.birdview.analysis.BVDocument

object BVDocumentUtils {
    fun getReferencedDocIds(filteredDocs: List<BVDocument>): Set<String> {
        val materializedIds = filteredDocs.flatMap { it.ids }.map { it.id }.toSet()
        val referencedIds = (filteredDocs.flatMap { it.relations.map{ it.ref } }).toSet()
        return referencedIds - materializedIds
    }
}