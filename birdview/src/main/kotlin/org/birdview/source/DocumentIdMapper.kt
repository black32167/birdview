package org.birdview.source

import org.slf4j.LoggerFactory

object DocumentIdMapper {
    private val log = LoggerFactory.getLogger(DocumentIdMapper::class.java)

    fun map(sourceType: SourceType, id: String) = when(sourceType) {
        SourceType.CONFLUENCE -> mapConfluenceId(id)
        else -> id
    }

    private fun mapConfluenceId(id: String): String {
        if (id.startsWith("http")) {
            if (id.contains("pageId=")) {
                val regex = "^(.*)/pages.*pageId=([0-9]*)".toRegex()
                val groups = regex.find(id)?.groups
                if (groups != null && groups.size >= 3) {
                    return "${groups.get(1)?.value}:${groups.get(2)?.value}"
                } else {
                    log.warn("Can't parse id: ${id}")
                }
            } else {
                val regex = "^(.*)/spaces/.*/pages/([0-9]*)".toRegex()
                val groups = regex.find(id)?.groups
                if (groups != null && groups.size >= 3) {
                    return "${groups.get(1)?.value}:${groups.get(2)?.value}"
                } else {
                    log.warn("Can't parse id: ${id}")
                }
            }
        }
        return id
    }
}