package org.birdview.web.source

import org.birdview.config.BVAbstractSourceConfig
import org.birdview.config.BVSourcesConfigProvider

abstract class AbstractSourceWebController<T : BVAbstractSourceConfig> (
        private val sourcesConfigProvider: BVSourcesConfigProvider
) {
    protected fun saveConfig(config:T) {
        sourcesConfigProvider.save(config)
    }

    protected fun updateConfig(config:T) {
        sourcesConfigProvider.update(config)
    }

    protected fun  getConfig(sourceName: String): T? =
            sourcesConfigProvider.getConfigByName(sourceName, getConfigClass())

    protected abstract fun getConfigClass(): Class<T>
}