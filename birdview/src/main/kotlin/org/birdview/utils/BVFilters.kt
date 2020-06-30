package org.birdview.utils

object BVFilters {
    val JIRA_KEY_REGEX = "\\w+-\\d+".toRegex()
    private val UUID1 = "\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b".toRegex()
    private val ALPHANUMERIC_ID = "([0-9]+[a-zA-Z]+[0-9a-zA-Z]*|[a-zA-Z]+[0-9]+[0-9a-zA-Z]*)".toRegex()

    fun filterIdsFromText(text: String): Set<String> {
        val ids = mutableSetOf<String>()
        ids.addAll(JIRA_KEY_REGEX.findAll (text).map { it.value })
        ids.addAll(UUID1.findAll(text).map { it.value })
        ids.addAll(ALPHANUMERIC_ID.findAll(text).map { it.value })
        return ids
    }
}
