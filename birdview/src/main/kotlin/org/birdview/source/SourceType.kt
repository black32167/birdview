package org.birdview.source

enum class SourceType(val alias:String) {
    UNDEFINED(SourceTypeNames.UNDEFINED),
    JIRA(SourceTypeNames.JIRA),
    GITHUB(SourceTypeNames.GITHUB),
    TRELLO(SourceTypeNames.TRELLO),
    GDRIVE(SourceTypeNames.GDRIVE),
    SLACK(SourceTypeNames.SLACK),
    CONFLUENCE(SourceTypeNames.CONFLUENCE),
    NONE(SourceTypeNames.NONE)
}

object SourceTypeNames {
    const val JIRA = "jira"
    const val GITHUB = "github"
    const val TRELLO = "trello"
    const val GDRIVE = "gdrive"
    const val SLACK = "slack"
    const val CONFLUENCE = "confluence"
    const val NONE = "none"
    const val UNDEFINED = "undefined"
}