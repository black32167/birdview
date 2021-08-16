package org.birdview.storage

import org.birdview.source.BVTaskSource
import org.birdview.source.SourceType
import javax.inject.Named

/**
 * Document provider manager
 */
@Named
class BVSourcesProvider (
    private val sourceConfigStorage: BVUserSourceConfigStorage,
    private val sourceManagers: List<BVTaskSource>
) {
    private val sourceManagersMap = sourceManagers.associateBy { it.getType() }

    fun listAvailableSourceNames(): List<String> =
        sourceManagers.map { it.getType().name.toLowerCase() }

    fun getBySourceName(bvUser: String, sourceName: String): BVTaskSource? =
            sourceConfigStorage.getSource(bvUser, sourceName)
                    ?.sourceType
                    ?.let { sourceManagersMap[it] }

    fun guessSourceTypesByDocumentId(id: String): SourceType? =
            sourceManagersMap.values.find { it.canHandleId(id) }?.getType()

    fun getBySourceType(sourceType: SourceType): BVTaskSource =
            sourceManagersMap[sourceType] ?: throw NoSuchElementException("Unknown source type ${sourceType}")
}