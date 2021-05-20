package org.radarbase.jersey.util

import org.slf4j.LoggerFactory

object Logging {
    fun initLog4J2() {
        try {
            val logManagerClass = Class.forName("org.apache.logging.log4j.jul.LogManager")
            System.setProperty("java.util.logging.manager", logManagerClass.name)
        } catch (ex: ClassNotFoundException) {
            LoggerFactory.getLogger(Logging::class.java).error("Cannot initialize full Log4J 2 logging. Please include the org.apache.logging.log4j:log4j-jul package as a runtime dependency.")
        }
    }

    fun initLogback() {
        try {
            val slf4jHandler = Class.forName("org.slf4j.bridge.SLF4JBridgeHandler");
            slf4jHandler.getMethod("removeHandlersForRootLogger").invoke(null);
            slf4jHandler.getMethod("install").invoke(null);
        } catch (ex: ClassNotFoundException) {
            LoggerFactory.getLogger(Logging::class.java).error("Cannot initialize full Logback logging. Please include the org.slf4j:jul-to-slf4j package as a runtime dependency.")
        }
    }
}
