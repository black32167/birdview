package org.birdview.storage.sql

import org.birdview.analysis.BVDocument
import org.birdview.model.BVDocumentFilter
import org.birdview.model.BVDocumentRef
import org.birdview.storage.BVDocumentStorage
import org.slf4j.LoggerFactory
import javax.inject.Named

@Named
class BVJpaDocumentStorage (
    val jpaRepository: BVJpaDocumentRepository
) : BVDocumentStorage {
    private val log = LoggerFactory.getLogger(BVJpaDocumentStorage::class.java)
    init {
        log.info("Init BVJpaDocumentStorage")
    }

    override fun findDocuments(filter: BVDocumentFilter): List<BVDocument> {
        TODO("Not yet implemented")
    }

    override fun getDocuments(searchingDocsIds: Collection<String>): List<BVDocument> {
        TODO("Not yet implemented")
    }

    override fun updateDocument(doc: BVDocument) {
        TODO("Not yet implemented")
    }

    override fun count(): Int {
        TODO("Not yet implemented")
    }

    override fun containsDocWithExternalId(externalId: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun getIncomingRefsByExternalIds(externalIds: Set<String>): List<BVDocumentRef> {
        TODO("Not yet implemented")
    }
}