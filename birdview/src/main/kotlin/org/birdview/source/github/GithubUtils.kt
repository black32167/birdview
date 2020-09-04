package org.birdview.source.github

import org.birdview.utils.BVDateTimeUtils

object GithubUtils {
    fun parseDate(date:String) =
            BVDateTimeUtils.parse(date, "yyyy-MM-dd'T'HH:mm:ss'Z'")

}