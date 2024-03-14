package org.radarbase.jersey.coroutines

import org.glassfish.jersey.server.monitoring.ApplicationEvent
import org.glassfish.jersey.server.monitoring.ApplicationEventListener
import org.glassfish.jersey.server.monitoring.RequestEvent
import org.glassfish.jersey.server.monitoring.RequestEventListener
import org.radarbase.jersey.service.AsyncCoroutineService

/** Listen for application events. */
abstract class AsyncApplicationEventListener(
    private val asyncService: AsyncCoroutineService,
) : ApplicationEventListener {
    override fun onEvent(event: ApplicationEvent?) {
        event ?: return
        asyncService.runBlocking {
            process(event)
        }
    }

    /**
     * Process incoming events. Inside processEvent a request scope is already present
     * so repositories can be accessed.
     */
    protected abstract suspend fun process(event: ApplicationEvent)

    override fun onRequest(requestEvent: RequestEvent?): RequestEventListener? = null
}
