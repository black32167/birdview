package org.birdview.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.OffsetDateTime
class BVDateTimeUtilsTest {
    @Test
    fun testParseWhenNoOffset() {
        val parsed = BVDateTimeUtils.parse("2021-09-10T05:41:26.117Z", "yyyy-MM-dd'T'HH:mm:ss.SSSX")
        assertThat(parsed).isEqualTo(OffsetDateTime.parse("2021-09-10T05:41:26.117Z"))
    }
}
