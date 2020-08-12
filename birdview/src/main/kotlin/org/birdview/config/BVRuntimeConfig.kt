package org.birdview.config

import org.springframework.beans.factory.annotation.Value
import java.nio.file.Path
import javax.inject.Named

@Named
class BVRuntimeConfig (
        private @Value("\${config.location}") val sourcesConfigFolder: Path) {
    val sourcesConfigsFolder = sourcesConfigFolder.resolve("sources")
    val usersConfigFileName = sourcesConfigFolder.resolve("bv-users.json")
    val oauthTokenDir = sourcesConfigFolder.resolve("tokens")
}