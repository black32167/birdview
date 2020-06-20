package org.birdview.source.trello

import org.birdview.config.BVTrelloConfig
import org.birdview.config.BVUsersConfigProvider
import org.birdview.source.BVTaskListsDefaults
import javax.inject.Named

@Named
class TrelloClientProvider(
        private val taskListDefaults: BVTaskListsDefaults,
        private val usersConfigProvider: BVUsersConfigProvider
) {
    fun getTrelloClient(trelloConfig: BVTrelloConfig) =
            TrelloClient(trelloConfig, taskListDefaults, usersConfigProvider)

}