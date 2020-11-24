package org.birdview.source

import org.birdview.analysis.BVDocument
import org.birdview.model.BVDocumentRef
import org.birdview.model.RelativeHierarchyPosition
import java.util.*

class BVDocumentsRelation (
        val parent: BVDocument,
        val child: BVDocument
) {
    companion object {
        fun getPriority(sourceType: SourceType): Int = when (sourceType) {
            SourceType.JIRA, SourceType.TRELLO -> 1
            SourceType.SLACK -> 2
            SourceType.GDRIVE, SourceType.CONFLUENCE -> 3
            SourceType.GITHUB -> 4
            SourceType.UNDEFINED -> 100
        }

        fun from(referncedDoc: BVDocument, doc: BVDocument, rel: BVDocumentRef): BVDocumentsRelation? =
            when (rel.hierarchyPosition) {
                RelativeHierarchyPosition.UNSPECIFIED -> {
                    val sourceTypePriorityDiff = signInt(getPriority(referncedDoc.sourceType) - getPriority(doc.sourceType))
                    val diff = sourceTypePriorityDiff.takeIf { it != 0 }
                            ?: signLong(millis(referncedDoc.created) - millis(doc.created))
                    when(diff) {
                        -1 -> BVDocumentsRelation(referncedDoc, doc)
                        1 -> BVDocumentsRelation(doc, referncedDoc)
                        else  -> null
                    }
                }
                RelativeHierarchyPosition.LINK_TO_PARENT -> BVDocumentsRelation(referncedDoc, doc)
                RelativeHierarchyPosition.LINK_TO_CHILD -> BVDocumentsRelation(doc, referncedDoc)
            }

        private fun signInt(x: Int): Int =
                signLong(x.toLong())

        private fun signLong(x: Long): Int =
                if (x < 0) {
                    -1
                } else if (x > 0) {
                    1
                } else {
                    0
                }

        private fun millis(created: Date?): Long =
                created?.time ?: 0
    }
}
