package org.birdview.source.confluence.model

import com.fasterxml.jackson.annotation.JsonProperty

class ConfluenceVersion(
        @JsonProperty("when")
        val _when: String,
        val by: ConfluenceUser
)