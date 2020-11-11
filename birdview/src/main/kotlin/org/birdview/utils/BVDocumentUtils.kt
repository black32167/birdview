package org.birdview.utils

import org.birdview.analysis.BVDocument

object BVDocumentUtils {
    fun getReferencedDocIds(filteredDocs: List<BVDocument>): Set<String> {
        val materializedIds = filteredDocs.flatMap { it.ids }.map { it.id }.toSet()
        val referencedIds = (filteredDocs.flatMap { it.refs.map{ it.ref } } + filteredDocs.flatMap { it.groupIds }.map { it.id }).toSet()
        return referencedIds - materializedIds
    }
}