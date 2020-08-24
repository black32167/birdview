package org.birdview.utils

object BVFilters {
    val JIRA_KEY_REGEX = "\\w+-\\d+".toRegex()
    private val UUID1 = "\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b".toRegex()
    private val ALPHANUMERIC_ID = "([0-9]+[a-zA-Z]+[0-9a-zA-Z]*|[a-zA-Z]+[0-9]+[0-9a-zA-Z]*)".toRegex()

    private val idPatterns = listOf(JIRA_KEY_REGEX, UUID1, ALPHANUMERIC_ID)

    fun filterIdsFromText(vararg texts: String): Set<String> {
        return texts.asSequence()
                .map { text -> idPatterns
                        .flatMap { pattern -> pattern.findAll (text).map { it.value }.toList() }
                        .toSet()
                }
                .firstOrNull { it.isNotEmpty() }
                ?: emptySet()
    }

    fun removeIdsFromText(text: String): String {
        return idPatterns.foldRight(text) { pattern, acc-> acc.replace(pattern, "") }
                .trim()
                .capitalize()
    }
}
