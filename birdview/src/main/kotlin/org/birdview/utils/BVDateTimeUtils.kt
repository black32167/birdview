package org.birdview.utils

import org.birdview.model.TimeIntervalFilter
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.util.*

object BVDateTimeUtils {
    private val log = LoggerFactory.getLogger(BVDateTimeUtils::class.java)
    private val formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val formatterDate = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    private val zonedFormatterDateTime = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss Z")

    fun format(interval: TimeIntervalFilter) = formatterDate.run {
        "${(interval.after?.let { dateTimeFormat(it) }?: "Now") to (interval.before?.let { dateTimeFormat(it) }?: "Now")}"
    }

    fun dateTimeFormat(instant: ZonedDateTime): String =
        instant.let(zonedFormatterDateTime::format)

    fun timeFormat(instant: TemporalAccessor?): String =
            instant?.let(formatterTime::format) ?: "Now"

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

    fun parse(serializedDateTime: String): ZonedDateTime =
        ZonedDateTime.parse(serializedDateTime, zonedFormatterDateTime)
}