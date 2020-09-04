package org.birdview.source.github.gql.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
class GqlGithubReviewRequest (
    val requestedReviewer: GqlGithubReviewer?
)

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "__typename",
        visible = true,
        defaultImpl = GqlGithubReviewer::class
)
@JsonSubTypes(
        JsonSubTypes.Type(value = GqlGithubReviewUser::class, name = "User")
)
open class GqlGithubReviewer (
        @JsonProperty("__typename") val type: String
)

class GqlGithubReviewUser (
        val login:String
): GqlGithubReviewer("User")