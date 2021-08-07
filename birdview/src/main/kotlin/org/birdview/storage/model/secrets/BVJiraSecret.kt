package org.birdview.storage.model.secrets

import org.birdview.source.SourceType

class BVJiraSecret (
        sourceName: String = "jira",
        val baseUrl: String,
        user: String,
        val token: String
): BVAbstractSourceSecret(SourceType.JIRA, sourceName, user)