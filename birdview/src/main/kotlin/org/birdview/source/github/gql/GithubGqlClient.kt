package org.birdview.source.github.gql

import org.apache.tomcat.util.http.fileupload.util.Streams
import org.birdview.source.github.gql.model.GqlGithubPullRequest
import org.birdview.source.github.gql.model.GqlGithubResponse
import org.birdview.source.github.gql.model.GqlGithubSearchContainer
import org.birdview.source.github.gql.model.GqlGithubUser
import org.birdview.storage.BVGithubConfig
import org.birdview.utils.BVTimeUtil
import org.birdview.utils.remote.BasicAuth
import org.birdview.utils.remote.ResponseValidationUtils
import org.birdview.utils.remote.WebTargetFactory
import org.slf4j.LoggerFactory
import javax.ws.rs.client.Entity
import javax.ws.rs.core.GenericType

class GithubGqlClient (
        private val githubConfig: BVGithubConfig
) {
    private val log = LoggerFactory.getLogger(GithubGqlClient::class.java)
    private class GQL(
            val query: String
    )
    fun getPullRequests(githubQuery: String, chunkConsumer: (List<GqlGithubPullRequest>) -> Unit) {
        log.info("Running Github query:{}", githubQuery)
        return BVTimeUtil.logTime("getPullRequests-GQL") {

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
                        BVTimeUtil.logTime("getPullRequests-GQL-page") {
                            getTarget()
                                    .request()
                                    .post(Entity.json(GQL(query)))
                                    .also(ResponseValidationUtils::validate)
                                    .readEntity(object : GenericType<GqlGithubResponse<GqlGithubSearchContainer<GqlGithubPullRequest>>>() {})
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

    fun getUserByEmail(email: String): String? {
        val gqlQuery = javaClass
                .getResourceAsStream("/github/gql/search-user.gql")
                .let(Streams::asString)
                .let {
                    interpolate(it, mapOf("query" to "${email} in:email"))
                }
        val response = getTarget()
                .request()
                .post(Entity.json(GQL(gqlQuery)))
                .also(ResponseValidationUtils::validate)
                .readEntity(object : GenericType<GqlGithubResponse<GqlGithubSearchContainer<GqlGithubUser>>>() {})
        return response.data?.search?.edges?.firstOrNull()?.node?.login
    }

    private fun interpolate(queryTemplate: String, parameters: Map<String, String>):String {
        var query = queryTemplate
        parameters.forEach {(param, value)->
            query = query.replace("\$${param}", value)
        }
        return query
    }

    private fun getTarget() = getTarget(githubConfig.baseGqlUrl)

    private fun getTarget(url: String) =
            WebTargetFactory(url) {
                BasicAuth(githubConfig.user, githubConfig.token)
            }.getTarget("")
}