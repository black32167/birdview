package org.birdview.resolve

import org.birdview.analysis.BVDocumentId
import org.birdview.config.BVJiraConfig
import org.birdview.config.BVSourcesConfigProvider
import org.birdview.config.BVTrelloConfig
import org.birdview.source.jira.JiraClientProvider
import org.birdview.source.trello.TrelloClientProvider
import org.birdview.source.trello.TrelloTaskService
import javax.inject.Named

@Named
class GroupIdResolver(
        private val sourcesConfigProvider: BVSourcesConfigProvider,
        private val jiraClientProvider: JiraClientProvider,
        private val trelloClientProvider: TrelloClientProvider

) {
    fun describe(docIds: Collection<BVDocumentId>): Map<BVDocumentId, String> = docIds
            .groupBy { it.sourceName }.entries
            .map { (source, ids) -> describeIds(source, ids) }
            .fold(mutableMapOf<BVDocumentId, String>()) { acc, map -> acc.putAll(map); acc }

    private fun describeIds(sourceName: String, groupIds: List<BVDocumentId>) =
            sourcesConfigProvider.getConfigByName(sourceName)?.let { sourceConfig ->
                when (sourceConfig) {
                    is BVJiraConfig -> describeJiraId(sourceConfig, groupIds)
                    is BVTrelloConfig -> describeTrelloIds(sourceConfig, groupIds)
                    else -> null
                }
            } ?: mapOf()

    private fun describeJiraId(sourceConfig: BVJiraConfig, groupIds: List<BVDocumentId>): Map<BVDocumentId, String> {
        val issuesMap = jiraClientProvider
                .getJiraClient(sourceConfig)
                .loadIssues(groupIds.map { it.id })
                .associateBy { it.key }
        return groupIds.map { docGId -> docGId to (issuesMap[docGId.id]?.fields?.summary ?: "") }.toMap()
    }

    private fun describeTrelloIds(sourceConfig: BVTrelloConfig, groupIds: List<BVDocumentId>): Map<BVDocumentId, String> =
            groupIds
                    .groupBy { it.type }.entries
                    .map { (type, docIds) ->
                        when(type) {
                            TrelloTaskService.TRELLO_BOARD_TYPE -> describeTrelloBoardId(docIds, sourceConfig)
                            else -> mapOf<BVDocumentId, String>()
                        }
                    }
                    .fold(mutableMapOf<BVDocumentId, String>()) { acc, map -> acc.putAll(map); acc }

    private fun describeTrelloBoardId(boardIds: List<BVDocumentId>, sourceConfig: BVTrelloConfig): Map<BVDocumentId, String> {
        val boardsMap = trelloClientProvider
                .getTrelloClient(sourceConfig)
                .getBoards(boardIds.map { it.id })
                .associateBy { board->board.id }
        return boardIds.associateWith { boardsMap.get(it.id)?.name ?: "???" }
    }

}