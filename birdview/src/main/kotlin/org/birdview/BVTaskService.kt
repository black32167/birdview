package org.birdview

import org.birdview.analysis.BVDocument
import org.birdview.model.BVDocumentFilter
import org.birdview.model.RelativeHierarchyType
import org.birdview.storage.BVDocumentStorage
import org.birdview.utils.BVDocumentUtils.getReferencedDocIdsByHierarchyType
import org.birdview.utils.BVTimeUtil
import javax.inject.Named

@Named
open class BVTaskService(
        private val documentStorage: BVDocumentStorage
) {
    open fun getDocuments(filter: BVDocumentFilter): List<BVDocument> {
        BVTimeUtil.printStats()

        val filteredDocs = BVTimeUtil.logTimeAndReturn("Filtering documents") {
            documentStorage.findDocuments(filter)
        }

        if (filter.sourceName != "") {
            return filteredDocs
        }

        return filteredDocs + getReferencedDocs(filter.userFilter.userAlias, filteredDocs)
    }

    // TODO: non-optimal
    private fun getReferencedDocs(bvUser:String, filteredDocs: List<BVDocument>): List<BVDocument> {
        return documentStorage.getDocuments(
            bvUser,
            getReferencedDocIdsByHierarchyType(
                filteredDocs, setOf(RelativeHierarchyType.LINK_TO_PARENT)))
    }
}
