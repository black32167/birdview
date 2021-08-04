package org.birdview.storage

import org.birdview.source.BVTaskSource
import org.birdview.source.SourceType
import javax.inject.Named

/**
 * Document provider manager
 */
@Named
class BVDocumentProvidersManager (
        private val sourceSecretsStorage: BVSourceSecretsStorage,
        sourceManagers: List<BVTaskSource>
) {
    private val sourceManagersMap = sourceManagers.associateBy { it.getType() }

    fun getSourceType(sourceName: String): SourceType? =
            getBySourceName(sourceName)?.getType()

    fun getBySourceName(sourceName: String): BVTaskSource? =
            sourceSecretsStorage.getSecret(sourceName)
                    ?.sourceType
                    ?.let { sourceManagersMap[it] }

    fun guessSourceTypesByDocumentId(id: String): SourceType? =
            sourceManagersMap.values.find { it.canHandleId(id) }?.getType()

    fun getBySourceType(sourceType: SourceType): BVTaskSource =
            sourceManagersMap[sourceType] ?: throw NoSuchElementException("Unknown source type ${sourceType}")
}