package org.birdview.analysis

interface DocumentsRanker {
    fun rank(doc: BVDocument, corpus: List<BVDocument>)
}