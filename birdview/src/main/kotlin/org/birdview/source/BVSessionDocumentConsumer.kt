package org.birdview.source

import org.birdview.analysis.BVDocument

interface BVSessionDocumentConsumer {
    fun consume(documents: List<BVDocument>)
    fun isConsumed(externalId: String): Boolean
}