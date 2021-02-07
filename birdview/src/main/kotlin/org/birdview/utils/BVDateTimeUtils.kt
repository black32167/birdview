package org.birdview.utils

import org.birdview.model.TimeIntervalFilter
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

object BVDateTimeUtils {
    private val log = LoggerFactory.getLogger(BVDateTimeUtils::class.java)
    private val formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val formatterDate = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private val formatterDateTime = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
    private val offsetFormatterDateTime = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss Z")

    fun offsetFormat(interval: TimeIntervalFilter) =
        format(interval, offsetFormatterDateTime)

    fun localDateFormat(interval: TimeIntervalFilter) =
        format(interval, formatterDate)

    fun format(interval: TimeIntervalFilter, formatter:DateTimeFormatter) =
        "${(interval.after?.let (formatter::format)?: "Now")} to ${  (interval.before?.let (formatter::format)?: "Now")}"

    fun dateTimeFormat(instant: OffsetDateTime): String =
        instant.let(offsetFormatterDateTime::format)

    fun timeFormat(instant: TemporalAccessor?): String =
            instant?.let(formatterTime::format) ?: "Now"

    fun format(maybeDate: ZonedDateTime?, format:String): String? =
            maybeDate?.format(DateTimeFormatter.ofPattern(format))

    fun parse(maybeDateTimeString: String?, pattern: String): OffsetDateTime? = try {
        maybeDateTimeString?.let { dateTimeString->
            OffsetDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(pattern))
        }
    } catch (e: Exception) {
        log.error("Error parsing date '{}'", maybeDateTimeString, e)
        null
    }

    fun offsetParse(serializedDateTime: String): OffsetDateTime =
        OffsetDateTime.parse(serializedDateTime, offsetFormatterDateTime)
}