package org.radarbase.jersey.coroutines

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

open class CoroutineRequestContext(
    val requestContext: org.glassfish.jersey.process.internal.RequestContext,
) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<CoroutineRequestContext>
}
