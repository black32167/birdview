package org.birdview.storage.model.source.secrets

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "_secretType")
@JsonSubTypes(
    JsonSubTypes.Type(value = BVTokenSourceSecret::class, name = "token"),
    JsonSubTypes.Type(value = BVOAuthSourceSecret::class, name = "oauth"),
    JsonSubTypes.Type(value = BVNoSecret::class, name = "none")
)
interface BVSourceSecret