package org.radarbase.jersey.hibernate

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.*
import org.radarbase.jersey.GrizzlyServer
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.config.ConfigLoader
import org.radarbase.jersey.hibernate.config.DatabaseConfig
import org.radarbase.jersey.hibernate.db.ProjectDao
import org.radarbase.jersey.hibernate.mock.MockResourceEnhancerFactory
import org.radarbase.kotlin.coroutines.forkJoin
import java.io.IOException
import java.io.InterruptedIOException
import java.net.URI
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class HibernateTest {
    private lateinit var client: OkHttpClient
    private lateinit var server: GrizzlyServer

    @BeforeEach
    fun setUp() {
        val authConfig = AuthConfig(
            jwtResourceName = "res_jerseyTest",
        )
        val databaseConfig = DatabaseConfig(
            managedClasses = listOf(ProjectDao::class.qualifiedName!!),
            driver = "org.h2.Driver",
            url = "jdbc:h2:mem:test",
            dialect = "org.hibernate.dialect.H2Dialect",
        )

        val resources = ConfigLoader.loadResources(MockResourceEnhancerFactory::class.java, authConfig, databaseConfig)

        server = GrizzlyServer(URI.create("http://localhost:9091"), resources)
        server.start()

        client = OkHttpClient()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun testBasicGet() {
        client.call {
            url("http://localhost:9091/projects")
        }.use { response ->
            assertThat(response.isSuccessful, `is`(true))
            assertThat(response.body?.string(), equalTo("[]"))
        }
    }

    @Test
    fun testMissingProject() {
        client.call {
            url("http://localhost:9091/projects/1")
        }.use { response ->
            assertThat(response.isSuccessful, `is`(false))
            assertThat(response.code, `is`(404))
        }
    }

    @Test
    fun testCancellation() {
        client = OkHttpClient.Builder()
            .callTimeout(Duration.ofMillis(250))
            .build()
        assertThrows<InterruptedIOException> {
            client.call {
                post("test".toRequestBody(JSON_TYPE))
                url("http://localhost:9091/projects/query")
            }
        }
    }

    @Test
    @Timeout(2)
    fun testOverload(): Unit = runBlocking {
        client = OkHttpClient.Builder()
            .connectionPool(ConnectionPool(64, 30, TimeUnit.MINUTES))
            .dispatcher(
                Dispatcher().apply {
                    maxRequestsPerHost = 64
                },
            )
            .build()
        (0 until 64)
            .forkJoin { i ->
                suspendCancellableCoroutine { continuation ->
                    val call = client.newCall(
                        Request.Builder().run {
                            post("test".toRequestBody(JSON_TYPE))
                            url("http://localhost:9091/projects/query")
                            build()
                        },
                    )

                    continuation.invokeOnCancellation { call.cancel() }

                    call.enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            continuation.resumeWithException(e)
                        }

                        override fun onResponse(call: Call, response: Response) {
                            continuation.resume(response)
                        }
                    })
                }
                println("$i finished")
            }
    }

    @Test
    fun testExistingGet() {
        client.call {
            post("""{"name": "a","organization":"main"}""".toRequestBody(JSON_TYPE))
            url("http://localhost:9091/projects")
        }.use { response ->
            assertThat(response.isSuccessful, `is`(true))
            assertThat(response.body?.string(), equalTo("""{"id":1000,"name":"a","organization":"main"}"""))
        }

        client.newCall(
            Request.Builder()
                .url("http://localhost:9091/projects/1000")
                .build(),
        ).execute().use { response ->
            assertThat(response.isSuccessful, `is`(true))
            assertThat(response.body?.string(), equalTo("""{"id":1000,"name":"a","organization":"main"}"""))
        }

        client.call {
            url("http://localhost:9091/projects")
        }.use { response ->
            assertThat(response.isSuccessful, `is`(true))
            assertThat(response.body?.string(), equalTo("""[{"id":1000,"name":"a","organization":"main"}]"""))
        }

        client.call {
            post("""{"name": "a","description":"d","organization":"main"}""".toRequestBody(JSON_TYPE))
            url("http://localhost:9091/projects/1000")
        }.use { response ->
            assertThat(response.isSuccessful, `is`(true))
            assertThat(response.body?.string(), equalTo("""{"id":1000,"name":"a","description":"d","organization":"main"}"""))
        }
        client.call {
            delete()
            url("http://localhost:9091/projects/1000")
        }.use { response ->
            assertThat(response.isSuccessful, `is`(true))
        }

        client.call {
            url("http://localhost:9091/projects")
        }.use { response ->
            assertThat(response.isSuccessful, `is`(true))
            assertThat(response.body?.string(), equalTo("[]"))
        }
    }

    companion object {
        val JSON_TYPE = "application/json".toMediaType()
    }
}

internal inline fun OkHttpClient.call(builder: Request.Builder.() -> Unit): Response = newCall(
    Request.Builder().run {
        builder()
        build()
    },
).execute()
