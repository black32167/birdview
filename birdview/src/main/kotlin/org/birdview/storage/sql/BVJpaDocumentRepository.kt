package org.birdview.storage.sql

import org.birdview.storage.sql.model.BVJpaDocument
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BVJpaDocumentRepository: CrudRepository<BVJpaDocument, String> {
}