package org.birdview.source.trello

import org.birdview.config.BVTrelloConfig
import org.birdview.source.BVTaskListsDefaults
import javax.inject.Named

@Named
class TrelloClientProvider(
        private val taskListDefaults: BVTaskListsDefaults
) {
    fun getTrelloClient(trelloConfig: BVTrelloConfig) =
            TrelloClient(trelloConfig, taskListDefaults)
}