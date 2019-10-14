package org.radarbase.jersey.filter

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

internal class ResponseLoggerFilterTest {
    @Test
    fun testTime() {
        val dateTime = LocalDateTime.parse("2019-01-02T12:13:14.163")

        assertThat(ResponseLoggerFilter.dateTimeFormatter.format(dateTime), equalTo("2019-01-02 12:13:14"))
    }

    @Test
    fun testInstant() {
        val fromInstant = LocalDateTime.ofInstant(Instant.parse("2019-01-02T12:13:14.163Z"), ZoneOffset.UTC)

        assertThat(ResponseLoggerFilter.dateTimeFormatter.format(fromInstant), equalTo("2019-01-02 12:13:14"))
    }
}