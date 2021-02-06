package org.birdview.source.github

import org.birdview.utils.BVDateTimeUtils
import java.time.OffsetDateTime

object GithubUtils {
    fun parseDate(date:String): OffsetDateTime? =
            BVDateTimeUtils.parse(date, "yyyy-MM-dd'T'HH:mm:ssX")

}