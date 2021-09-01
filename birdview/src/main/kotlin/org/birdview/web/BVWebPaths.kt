package org.birdview.web

object BVWebPaths {
    const val ADMIN_ROOT = "/admin"
    const val SECRETS = "${ADMIN_ROOT}/secrets"
    const val USER_ROOT = "/user"
    const val EXPLORE = "${USER_ROOT}/explore"
    const val USER_SETTINGS = "${USER_ROOT}/settings"
    const val USER_SOURCE = "${USER_ROOT}/source"
    const val LOGIN = "/login"
    const val SIGNUP = "/signup"
    const val OAUTH_CODE_ENDPOINT_PATH = "/oauth/code"
}