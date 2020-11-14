package org.birdview.source.github.gql.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "__typename",
        visible = true,
        defaultImpl = GqlGithubActor::class
)
@JsonSubTypes(
        JsonSubTypes.Type(value = GqlGithubUserActor::class, name = "User")
)
open class GqlGithubActor (
        @JsonProperty("__typename") val type: String?
)

class GqlGithubUserActor (
        val login:String
) : GqlGithubActor("User")