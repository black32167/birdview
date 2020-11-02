package org.birdview.source.confluence.model

class ConfluenceHistory (
        val latest: Boolean,
        val createdBy: ConfluenceUser,
        val contributors: ConfluenceContributors
)