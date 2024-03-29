package org.birdview.utils

import org.birdview.analysis.BVDocumentId
import org.birdview.source.DocumentIdMapper
import org.birdview.source.SourceType

object BVFilters {
    val JIRA_KEY_REGEX = "[A-Z]+-\\d+".toRegex()
    private val EMPTY_BRACKETS = "\\[\\] *:? *".toRegex()
    private val UUID1 = "\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b".toRegex()
    private val ALPHANUMERIC_ID = "([0-9]+[a-zA-Z]+[0-9a-zA-Z]*|[a-zA-Z]+[0-9]+[0-9a-zA-Z]*)".toRegex()
    private var URL = "https?://[-a-zA-Z0-9+&@#/%?=~_:,.;]*[-a-zA-Z0-9+&@#/%=~_]".toRegex()
    private var CONFLUENCE_URL = "https?://.*/wiki/.*".toRegex()

    private val idPatterns = listOf(
            SourceType.JIRA to JIRA_KEY_REGEX,
            SourceType.JIRA to JIRA_KEY_REGEX,
            SourceType.UNDEFINED to UUID1,
            SourceType.UNDEFINED to ALPHANUMERIC_ID,
            SourceType.UNDEFINED to URL,
            SourceType.CONFLUENCE to CONFLUENCE_URL)

    fun filterRefsFromText(vararg texts: String): Set<BVDocumentId> {
        return texts.asSequence()
                .map { text -> idPatterns
                        .flatMap { (sourceType, pattern) ->
                            pattern.findAll(text)
                                    .map { it.value }
                                    .map { rawId -> DocumentIdMapper.map(sourceType, rawId) }
                                    .map { refinedId -> BVDocumentId(refinedId, sourceType = sourceType) }
                                    .toList()
                        }
                        .toSet()
                }
                .firstOrNull { it.isNotEmpty() }
                ?: emptySet()
    }

    fun removeIdsFromText(text: String): String {
        return idPatterns.foldRight(text) { (_, pattern), acc-> acc.replace(pattern, "") }
                .trim()
                .capitalize()
                .replace(EMPTY_BRACKETS, "")
    }
}
