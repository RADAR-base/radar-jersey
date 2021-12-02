package org.radarbase.jersey.enhancer

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.ws.rs.ext.ContextResolver
import org.glassfish.jersey.server.ResourceConfig
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

import java.time.Instant

internal class MapperResourceEnhancerTest {

    @Test
    fun getMapper() {
        val mapper = MapperResourceEnhancer.createDefaultMapper()
        assertThat(mapper.writeValueAsString(InstantWrapper()), equalTo("""{"date":"1970-01-01T01:00:00Z"}"""))
    }

    @Test
    fun enhanceResourceConfig() {
        val enhancer = MapperResourceEnhancer()
        val resourceConfig = mock<ResourceConfig>()
        enhancer.enhanceResources(resourceConfig)
        verify(resourceConfig).register(check<Any> { obj ->
            val context = obj as ContextResolver<*>
            val mapper = context.getContext(ObjectMapper::class.java) as ObjectMapper
            assertThat(mapper.writeValueAsString(InstantWrapper()), equalTo("""{"date":"1970-01-01T01:00:00Z"}"""))
        })
    }

    data class InstantWrapper(
        val date: Instant = Instant.ofEpochSecond(3600)
    )
}
