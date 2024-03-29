package org.birdview.utils.remote

sealed class ApiAuth
class BearerAuth(val bearerToken: String): ApiAuth()
class BasicAuth(val user: String, val password: String): ApiAuth()
class ParameterAuth(val key: String, val token: String): ApiAuth()