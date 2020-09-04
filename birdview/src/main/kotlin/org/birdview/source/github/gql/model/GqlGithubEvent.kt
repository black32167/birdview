package org.birdview.source.github.gql.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.birdview.analysis.BVDocumentOperationType

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "__typename",
        visible = true,
        defaultImpl = GqlGithubEvent::class
)
@JsonSubTypes(
        JsonSubTypes.Type(value = IssueComment::class, name = "IssueComment"),
        JsonSubTypes.Type(value = PullRequestCommit::class, name = "PullRequestCommit"),
        JsonSubTypes.Type(value = GqlGithubUser::class, name = "User")
)
open class GqlGithubEvent (
        @JsonProperty("__typename") val type: String
) {
    open val contributionType: BVDocumentOperationType = BVDocumentOperationType.NONE
    open val timestamp: String? = null
    open val user: String? = null;
}

class PullRequestCommit(
        val commit: GqlGithubCommit,
        val url: String
) : GqlGithubEvent("PullRequestCommit") {
    override val contributionType: BVDocumentOperationType = BVDocumentOperationType.COLLABORATE
    override val timestamp = commit.committedDate
    override val user = commit.committer?.name
}

class IssueComment(
        val author: GqlGithubUser,
        val publishedAt: String
) : GqlGithubEvent("IssueComment") {
    override val contributionType: BVDocumentOperationType = BVDocumentOperationType.COMMENT
    override val timestamp = publishedAt
    override val user = author.login
}