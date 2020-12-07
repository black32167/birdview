package org.birdview.utils

import org.birdview.analysis.BVDocument

object BVDocumentUtils {
    fun getReferencedDocIds(filteredDocs: Collection<BVDocument>): Set<String> {
        val materializedIds = filteredDocs.flatMap { it.ids }.map { it.id }.toSet()
        val referencedIds = (filteredDocs.flatMap { it.refs.map{ it.docId.id } }).toSet()
        return referencedIds - materializedIds
    }
}