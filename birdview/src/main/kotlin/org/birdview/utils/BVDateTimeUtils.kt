package org.birdview.utils

import org.birdview.model.TimeIntervalFilter
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

object BVDateTimeUtils {
    private val log = LoggerFactory.getLogger(BVDateTimeUtils::class.java)
    private val formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val formatterDate = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    private val zonedFormatterDateTime = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss Z")

    fun format(interval: TimeIntervalFilter) = formatterDate.run {
        "${(interval.after?.let { dateTimeFormat(it) }?: "Now")} to ${  (interval.before?.let { dateTimeFormat(it) }?: "Now")}"
    }

    fun dateTimeFormat(instant: OffsetDateTime): String =
        instant.let(zonedFormatterDateTime::format)

    fun timeFormat(instant: TemporalAccessor?): String =
            instant?.let(formatterTime::format) ?: "Now"

    fun format(maybeDate: OffsetDateTime?, format:String): String? =
            maybeDate?.format(DateTimeFormatter.ofPattern(format))

    fun parse(maybeDateTimeString: String?, pattern: String): OffsetDateTime? = try {
        maybeDateTimeString?.let { dateTimeString->
            OffsetDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(pattern))
        }
    } catch (e: Exception) {
        log.error("Error parsing date '{}'", maybeDateTimeString, e)
        null
    }

    fun parse(serializedDateTime: String): OffsetDateTime =
        OffsetDateTime.parse(serializedDateTime, zonedFormatterDateTime)
}