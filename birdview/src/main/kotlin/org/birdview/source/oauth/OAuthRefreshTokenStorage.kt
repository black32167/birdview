package org.birdview.source.oauth

import org.birdview.config.BVOAuthSourceConfig
import org.birdview.config.BVRuntimeConfig
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import javax.inject.Named

@Named
class OAuthRefreshTokenStorage(
        val bvRuntimeConfig: BVRuntimeConfig
) {
    fun loadLocalRefreshToken(source: String):String? =
            readToken(getRefreshTokenFilePath(source))

    fun hasToken(config: BVOAuthSourceConfig): Boolean =
            loadLocalRefreshToken(config.sourceName) != null;

    fun saveRefreshToken(sourceName: String, refreshToken: String) {
        saveToken(getRefreshTokenFilePath(sourceName), refreshToken)
    }

    fun saveAccessToken(sourceName: String, accessToken: String) {
        saveToken(getAccessTokenFilePath(sourceName), accessToken)
    }

    fun getAccessToken(config: BVOAuthSourceConfig): String? =
        readToken(getAccessTokenFilePath(config.sourceName))

    private fun getRefreshTokenFilePath(source: String): Path =
            bvRuntimeConfig.oauthTokenDir.resolve("${source}.token")
    private fun getAccessTokenFilePath(sourceName: String): Path =
            bvRuntimeConfig.oauthTokenDir.resolve("${sourceName}.access.token")
    private fun readToken(filePath: Path):String? =
            filePath
                    .takeIf { Files.exists(it) }
                    ?.let { tokenFile -> Files.readAllLines(tokenFile).firstOrNull() }
    fun saveToken(filePath: Path, token: String) {
        Files.createDirectories(filePath.parent)
        Files.write(filePath, listOf(token),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }
}