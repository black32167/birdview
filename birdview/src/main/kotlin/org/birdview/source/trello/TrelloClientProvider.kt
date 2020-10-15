package org.birdview.source.trello

import org.birdview.source.BVTaskListsDefaults
import org.birdview.storage.BVTrelloConfig
import javax.inject.Named

@Named
class TrelloClientProvider(
        private val taskListDefaults: BVTaskListsDefaults
) {
    fun getTrelloClient(trelloConfig: BVTrelloConfig) =
            TrelloClient(trelloConfig, taskListDefaults)
}