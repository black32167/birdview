package org.birdview.config

import org.springframework.beans.factory.annotation.Value
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Named

@Named
class BVFoldersConfig (
        @Value("\${config.location}") private val configFolder: Path) {
    val sourcesSharedSecretsConfigsFolder = Files.createDirectories(configFolder.resolve("sources"))
    val usersConfigFolder = Files.createDirectories(configFolder.resolve("users"))
    val oauthTokenDir = Files.createDirectories(configFolder.resolve("tokens"))

    fun getHttpInteractionsLogFolder(): Path = Paths.get("/tmp/birdview/http")
    fun getHttpInteractionsReplayFolder(): Path = Paths.get("/tmp/birdview/http")

    fun getUserConfigFolder(userName: String): Path =
            usersConfigFolder.resolve(userName)

    fun getUserSourceConfigsFolder(userName: String): Path =
            getUserConfigFolder(userName).resolve("sources")
                    .also (this::createSingleDirectoryIfNotExists)

    fun getUserSourceTokensFolder(userName: String): Path =
            Files.createDirectories(getUserConfigFolder(userName).resolve("tokens"))
                    .also (this::createSingleDirectoryIfNotExists)

    private fun createSingleDirectoryIfNotExists(path:Path) {
        if(!Files.exists(path)) {
            Files.createDirectory(path)
        }
    }

}
