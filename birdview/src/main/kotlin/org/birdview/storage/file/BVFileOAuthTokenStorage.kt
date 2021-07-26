package org.birdview.storage.file

import org.birdview.BVProfiles
import org.birdview.config.BVFoldersConfig
import org.birdview.storage.OAuthTokenStorage
import org.birdview.storage.model.BVOAuthTokens
import org.springframework.context.annotation.Profile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import javax.inject.Named

@Profile(BVProfiles.FILESTORE)
@Named
class BVFileOAuthTokenStorage(
    val bvFoldersConfig: BVFoldersConfig
): OAuthTokenStorage {
    override fun loadOAuthTokens(sourceName: String): BVOAuthTokens? =
        readToken(getAccessTokenFilePath(sourceName))
            ?.let { accessToken->
                BVOAuthTokens(
                    accessToken = accessToken,
                    refreshToken = readToken(getRefreshTokenFilePath(sourceName)) ?: "",
                )
            }

    override fun saveOAuthTokens(sourceName: String, tokens: BVOAuthTokens) {
        saveToken(getAccessTokenFilePath(sourceName), tokens.accessToken)
        tokens.refreshToken?.also { refreshToken ->
            saveToken(getRefreshTokenFilePath(sourceName), refreshToken)
        }
    }

    private fun getRefreshTokenFilePath(source: String): Path =
        bvFoldersConfig.oauthTokenDir.resolve("${source}.token")
    private fun getAccessTokenFilePath(sourceName: String): Path =
        bvFoldersConfig.oauthTokenDir.resolve("${sourceName}.access.token")
    private fun readToken(filePath: Path):String? =
        filePath
            .takeIf { Files.exists(it) }
            ?.let { tokenFile -> Files.readAllLines(tokenFile).firstOrNull() }
    private fun saveToken(filePath: Path, token: String) {
        Files.createDirectories(filePath.parent)
        Files.write(filePath, listOf(token),
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }
}