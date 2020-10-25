package org.birdview.storage.memory

import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.model.BVDocumentFilter
import org.birdview.storage.BVDocumentStorage
import org.birdview.storage.BVSourceUserNameResolver
import org.birdview.storage.BVUserSourceStorage
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Named

@Named
class BVInMemoryDocumentStorage(
        private val userSourceStorage: BVUserSourceStorage
): BVDocumentStorage {
    // id -> doc
    private val docsMap = ConcurrentHashMap<BVDocumentId, BVDocument>()
    private val usersRetrieved = ConcurrentHashMap<String, Boolean>()

    override fun findDocuments(filter: BVDocumentFilter): List<BVDocument> {
        val predicate = BVDocumentPredicate(filter, object : BVSourceUserNameResolver {
            override fun resolve(bvUser: String, sourceName: String): String? {
                return userSourceStorage.getSourceProfile(bvUser, sourceName).sourceUserName
            }
        })
        return docsMap
                .values
                .filter { predicate.test(it) }
                .toMutableList()
    }

    override fun getDocuments(searchingDocsIds: Set<String>): List<BVDocument> =
        docsMap.values.filter { docId -> docId.ids.find { searchingDocsIds.contains(it.id) } != null }

    override fun updateDocument(id: BVDocumentId, doc: BVDocument) {
        docsMap[id] = doc
    }

}