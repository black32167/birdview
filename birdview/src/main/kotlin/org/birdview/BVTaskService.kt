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

        val filteredDocs = BVTimeUtil.logTime("Filtering documents") {
            documentStorage.findDocuments(filter)
        }

        if (filter.sourceType != "") {
            return filteredDocs
        }

        return filteredDocs + getReferencedDocs(filteredDocs)
    }

    // TODO: non-optimal
    private fun getReferencedDocs(filteredDocs: List<BVDocument>): List<BVDocument> {
        return documentStorage.getDocuments(
            getReferencedDocIdsByHierarchyType(
                filteredDocs, setOf(RelativeHierarchyType.LINK_TO_PARENT, RelativeHierarchyType.UNSPECIFIED)))
    }
}
