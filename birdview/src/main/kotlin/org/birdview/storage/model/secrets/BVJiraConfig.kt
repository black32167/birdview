package org.birdview.storage.model.secrets

import org.birdview.source.SourceType

class BVJiraConfig (
        sourceName: String = "jira",
        val baseUrl: String,
        user: String,
        val token: String
): BVAbstractSourceConfig(SourceType.JIRA, sourceName, user)