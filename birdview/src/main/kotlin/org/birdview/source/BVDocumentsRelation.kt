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

        fun from(doc1: BVDocument, doc2: BVDocument): BVDocumentsRelation? {
            val sourceTypePriorityDiff = signInt(getPriority(doc1.sourceType) - getPriority(doc2.sourceType))
            val diff = sourceTypePriorityDiff.takeIf { it != 0 }
                    ?: signLong(millis(doc1.created) - millis(doc2.created))
            return when(diff) {
                -1 -> BVDocumentsRelation(doc1, doc2)
                1 -> BVDocumentsRelation(doc2, doc1)
                else  -> null
            }
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
