package org.birdview.utils

import org.birdview.analysis.BVDocument
import org.birdview.model.RelativeHierarchyType
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object BVDocumentUtils {
    fun hashId(externalId: String) =
        MessageDigest.getInstance("SHA-256")
            .let { digest-> digest.digest(
                externalId.toByteArray(StandardCharsets.UTF_8)
            )}
            .let { bytes ->  bytes.joinToString(separator = "") { "%02x".format(it) } }

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