package org.birdview.utils

import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*

object BVDateTimeUtils {
    private val log = LoggerFactory.getLogger(BVDateTimeUtils::class.java)

    fun format(maybeDate: Date?, format:String): String? =
            maybeDate?.let { dateTimeString ->
                SimpleDateFormat(format)
                        .also { it.timeZone = TimeZone.getTimeZone("UTC") }
                        .format(dateTimeString)
            }

    fun parse(maybeDateTimeString: String?, pattern: String): Date? = try {
        maybeDateTimeString?.let { dateTimeString->
            SimpleDateFormat(pattern).parse(dateTimeString)
        }
    } catch (e: Exception) {
        log.error("Error parsing date '{}'", maybeDateTimeString, e)
        null
    }
}