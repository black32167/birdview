package org.birdview.source

import org.birdview.analysis.BVDocument
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
        }

        fun from(referncedDoc: BVDocument, doc: BVDocument, refType: BVRefType): BVDocumentsRelation? =
            when (refType) {
                BVRefType.UNSPECIFIED -> {
                    val sourceTypePriorityDiff = signInt(getPriority(referncedDoc.sourceType) - getPriority(doc.sourceType))
                    val diff = sourceTypePriorityDiff.takeIf { it != 0 }
                            ?: signLong(millis(referncedDoc.created) - millis(doc.created))
                    when(diff) {
                        -1 -> BVDocumentsRelation(referncedDoc, doc)
                        1 -> BVDocumentsRelation(doc, referncedDoc)
                        else  -> BVDocumentsRelation(doc, referncedDoc)
                    }
                }
                BVRefType.PAREN -> BVDocumentsRelation(referncedDoc, doc)
                BVRefType.CHILD -> BVDocumentsRelation(doc, referncedDoc)
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
