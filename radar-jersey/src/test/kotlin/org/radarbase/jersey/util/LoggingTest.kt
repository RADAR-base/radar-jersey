package org.radarbase.jersey.util

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory


internal class LoggingTest {
    @Test
    fun initLogging() {
        LoggerFactory.getLogger(LoggingTest::class.java).info("Success SLF4J + Backend")
        java.util.logging.LogManager.getLogManager().getLogger("global").info("Success SLF4J + JUL")
    }

    companion object {
        init {
            Logging.initLog4j2()
        }
    }
}
