package org.birdview.source.github.gql

import org.apache.tomcat.util.http.fileupload.util.Streams
import org.birdview.source.BVSourceConfigProvider
import org.birdview.source.github.gql.model.*
import org.birdview.source.http.BVHttpClient
import org.birdview.source.http.BVHttpSourceClientFactory
import org.birdview.utils.BVTimeUtil
import org.slf4j.LoggerFactory
import javax.inject.Named

@Named
class GithubGqlClient (
    private val httpClientFactory: BVHttpSourceClientFactory,
    private val sourceConfigProvider: BVSourceConfigProvider,
) {
    private val log = LoggerFactory.getLogger(GithubGqlClient::class.java)
    private class GQL(
            val query: String
    )
    fun getPullRequests(bvUser: String, sourceName: String, githubQuery: String, chunkConsumer: (List<GqlGithubPullRequest>) -> Unit) {
        log.info("Running Github query:{}", githubQuery)
        return BVTimeUtil.logTimeAndReturn("getPullRequests-GQL") {

            val queryTemplate = javaClass
                    .getResourceAsStream("/github/gql/search.gql")
                    .let(Streams::asString)
                    .let {
                        interpolate(it, mapOf("query" to githubQuery))
                    }

            var cursor: String? = null
            do {
                val query = interpolate(queryTemplate, mapOf(
                        "cursor" to (cursor?.let { "\"${cursor}\"" } ?: "null")))

                val response: GqlGithubResponse<GqlGithubSearchContainer<GqlGithubPullRequest>> =
                        BVTimeUtil.logTimeAndReturn("getPullRequests-GQL-page") {
                            getHttpClient(bvUser =  bvUser, sourceName = sourceName)
                                .post(
                                    GqlGithubSearchPullRequestResponse::class.java,
                                    GQL(query))
                                .also {
                                    if (it.errors.isNotEmpty()) {
                                        throw RuntimeException("Error: ${it.errors.map { "  ${it.message}" }.joinToString("\n")}")
                                    }
                                }
                        }
                val data = response.data!!
                val edges = data.search.edges //?.sortedBy { it.node.updatedAt }
                val pageInfo = data.search.pageInfo
                val prs = edges.map { it.node }
                log.info("Loaded {} pull requests", prs.size)
                if (prs.isNotEmpty()) {
                    chunkConsumer.invoke(prs)
                }
                cursor = pageInfo.endCursor
            } while (pageInfo.hasNextPage)
        }
    }

    fun getUserByEmail(bvUser: String, sourceName: String, email: String): String? {
        val gqlQuery = javaClass
                .getResourceAsStream("/github/gql/search-user.gql")
                .let(Streams::asString)
                .let {
                    interpolate(it, mapOf("query" to "${email} in:email"))
                }
        val response = getHttpClient(bvUser =  bvUser, sourceName = sourceName)
                .post(
                    GqlGithubSearchUserResponse::class.java,
                    GQL(gqlQuery))
        return response.data?.search?.edges?.firstOrNull()?.node?.login
    }

    private fun interpolate(queryTemplate: String, parameters: Map<String, String>):String {
        var query = queryTemplate
        parameters.forEach {(param, value)->
            query = query.replace("\$${param}", value)
        }
        return query
    }

    private fun getHttpClient(bvUser: String, sourceName: String): BVHttpClient {
        val sourceConfig = sourceConfigProvider.getSourceConfig(sourceName = sourceName, bvUser = bvUser)
        return httpClientFactory.createClient(bvUser = bvUser, sourceName = sourceName, url = "${sourceConfig.baseUrl}/graphql")
    }
}