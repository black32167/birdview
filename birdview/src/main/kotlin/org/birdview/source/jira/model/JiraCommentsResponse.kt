package org.birdview.source.jira.model

class JiraCommentsResponse (
    val comments: List<JiraComment>
)

class JiraComment (
    val updateAuthor: JiraUser,
    val updated: String
)