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
    override fun loadOAuthTokens(bvUser:String, sourceName: String): BVOAuthTokens? =
        readToken(getAccessTokenFilePath(bvUser = bvUser, sourceName = sourceName))
            ?.let { accessToken->
                BVOAuthTokens(
                    accessToken = accessToken,
                    refreshToken = readToken(getRefreshTokenFilePath(bvUser = bvUser, sourceName = sourceName)) ?: "",
                )
            }

    override fun saveOAuthTokens(bvUser:String, sourceName: String, tokens: BVOAuthTokens) {
        saveToken(getAccessTokenFilePath(bvUser = bvUser, sourceName = sourceName), tokens.accessToken)
        tokens.refreshToken?.also { refreshToken ->
            saveToken(getRefreshTokenFilePath(bvUser = bvUser, sourceName = sourceName), refreshToken)
        }
    }

    private fun getRefreshTokenFilePath(bvUser: String, sourceName: String): Path =
        bvFoldersConfig.getUserSourceTokensFolder(bvUser).resolve("${sourceName}.token")
    private fun getAccessTokenFilePath(bvUser: String, sourceName: String): Path =
        bvFoldersConfig.getUserSourceTokensFolder(bvUser).resolve("${sourceName}.access.token")
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