package org.birdview.storage

import org.birdview.source.BVTaskSource
import org.birdview.source.SourceType
import javax.inject.Named

@Named
class BVSourcesManager (
        private val sourceSecretsStorage: BVSourceSecretsStorage,
        sourceManagers: List<BVTaskSource>
) {
    private val sourceManagersMap = sourceManagers.associateBy { it.getType() }

    fun getBySourceName(sourceName: String): BVTaskSource? =
            sourceSecretsStorage.getConfigByName(sourceName)
                    ?.sourceType
                    ?.let { sourceManagersMap[it] }

    fun guessSourceTypesByDocumentId(id: String): SourceType? =
            sourceManagersMap.values.find { it.canHandleId(id) }?.getType()

    fun getBySourceType(sourceType: SourceType): BVTaskSource =
            sourceManagersMap[sourceType] ?: throw NoSuchElementException("Unknown source type ${sourceType}")
}