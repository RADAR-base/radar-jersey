# radar-jersey

Library to facilitate using with a Jersey-based REST API. This includes OAuth 2.0 integration, exception handling and resource configuration.

## Usage

Add this library to your project using the following Gradle configuration:
```gradle
repositories {
    mavenCentral()
}

dependencies {
    api("org.radarbase:radar-jersey:0.12.4")
}
```

Any path or resource that should be authenticated against the ManagementPortal, should be annotated with `@Authenticated`. Specific authorization can be checked by adding a `@NeedsPermission` annotation. An `Auth` object can be injected to get app-specific information. For reliable injection, constructor or method injection, not class parameter injection. Examples:

## Asynchronous coroutines & request scope
The library provides `AsyncCoroutineService` / `ScopedAsyncCoroutineService` and helpers
(`CoroutineRequestWrapper`, `CoroutineRequestContext`) so you can run resource handlers as Kotlin
coroutines and keep request-scoped injection available when needed.

However, request scope and coroutines interact in a subtle way. This section explains the rules,
gives safe examples, and lists best practices.

### Key concepts

- **`runInRequestScope { ... }` / `suspendInRequestScope { ... }`**: helpers that activate the
  `RequestContext` handle for the duration of a small block. Use these to run code that relies 
  on request-scoped beans eg: `Provider<ContainerRequestContext>`or other.


- `CoroutineRequestWrapper` stores the `RequestContext` handle in the coroutine context (as
  `CoroutineRequestContext`) and manages cancellation and `release()` for you. But note: *having the
  handle in the coroutine context does not automatically activate the request scope* — you must call
  `runInRequestScope` to do that.


- Snapshot vs scope activation — prefer snapshotting (copy the few request values you need on the request thread) when possible. Use runInRequestScope only for short blocks that require actual request-scoped DI.

### Quick usage examples
```kotlin
@Path("/projects")
@Authenticated
class Users(
    @Context private val projectService: MyProjectService,
    @Context private val asyncService: AsyncCoroutineService,
    @Context private val authService: AuthService,
) {
    // Most services can be run as coroutines with
    // asynchronous handling
    // Basic: simple coroutine handler 
    // (preferred when no request-scoped DI needed)
    @GET
    @NeedsPermission(Permission.PROJECT_READ)
    fun getProjects(
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        // projectService.read() is not request-scoped.
        // safe to use directly in coroutine.
        projectService.read()
            .filter { authService.hasPermission(PROJECT_READ, entityDetails { project(it.name) }) }
    }

    @POST
    @Path("/{projectId}")
    @NeedsPermission(Permission.PROJECT_UPDATE, "projectId")
    fun updateProject(
        @PathParam("projectId") projectId: String,
        project: Project,
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        projectService.update(projectId, project)
    }

    @GET
    @Path("/{projectId}/users/{userId}")
    @NeedsPermission(Permission.SUBJECT_READ, "projectId", "userId")
    fun getUsers(
        @PathParam("projectId") projectId: String,
        @PathParam("userId") userId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        projectService.readUser(projectId, userId)
    }

    // Simple responses can be handled without context switches
    @GET
    @Path("/{projectId}/settings")
    @NeedsPermission(Permission.PROJECT_READ, "projectId")
    fun getProjectSettings(
        @PathParam("projectId") projectId: String,
    ): ProjectSettingsDto {
        return ProjectSettingsDto(projectId = projectId)
    }

    // Simple coroutine responses can also handled without context switches.
    @GET
    @Path("/{projectId}/users/{userId}/settings")
    @NeedsPermission(Permission.SUBJECT_READ, "projectId", "userId")
    fun getProjectSettings(
        @PathParam("projectId") projectId: String,
        @PathParam("userId") userId: String,
    ) = asyncService.runBlocking {
        // Use asyncService.runBlocking { ... } when you want the convenience of
        // coroutine syntax in a synchronous resource method:
        UserSettingsDto(projectId = projectId, userId = userId)
    }
}
```
### What `AsyncCoroutineService` does under the hood 
The `ScopedAsyncCoroutineService` and `CoroutineRequestWrapper` prepare a coroutine context
that carries a request-context handle, wire timeouts and connection callbacks, and ensure 
request-context handles are released on completion/cancel/timeouts. You should use the 
library helpers rather than HK2 internals.

- When starting a coroutine via `runAsCoroutine`, the library prepares a coroutine context 
  that carries a request-context handle (this handle either represents the real current
  request scope or a created context -- the wrapper chooses what’s appropriate).


- **Carrying the handle is not the same as activating the scope** Use `runInRequestScope`
  to temporarily re-activate it for the code that needs request-scoped injection.


- The library registers connection callbacks and timeouts and ensures `release()` is called
  for the request-context handle when the coroutine
  finishes or is cancelled. This avoids 
  most common leaks when used as intended. 

### Troubleshooting (common error and what it means)
- **Not inside a request scope**: You attempted to access request-scoped objects
  (ContainerRequestContext, UriInfo, request-scoped beans) from code that is not
  running with an activated request scope. Fixes:
    - capture necessary request values on the request thread (snapshot), or
    - call runInRequestScope { ... } in your coroutine to activate the request scope for the small block that needs request-scoped DI.

---

These APIs are activated by adding an `EnhancerFactory` implementation to your resource definition:
```kotlin
class MyEnhancerFactory(private val config: MyConfigClass): EnhancerFactory {
    override fun createEnhancers(): List<JerseyResourceEnhancer> {
        val authConfig = AuthConfig(
            managementPortal = MPConfig(
                url = "https://...",
            ),
            jwtResourceName = "res_MyResource",
        )
        return listOf(
            // My own resource configuration
            MyResourceEnhancer(),
            // RADAR OAuth2 enhancement
            Enhancers.radar(authConfig),
            // Use ManagementPortal OAuth implementation
            Enhancers.managementPortal(authConfig),
            // Error handling
            Enhancers.exception,
        )
    }

    class MyResourceEnhancer: JerseyResourceEnhancer {
        // only classes used directly by Jersey, cannot inject them in user code
        override val classes: Array<Class<*>> = arrayOf(
            Filters.logResponse,
            Filters.cors,
            Filters.cache,
        )

        // only classes used directly by Jersey, cannot inject them in user code
        override val packages = arrayOf(
            "com.example.app.resources",
        )

        override fun AbstractBinder.enhance() {
            bind(config)
                .to(MyConfigClass::class.java)
            bind(MyService::class.java)
                .to(MyServiceInterface::class.java)
                .`in`(Singleton::class.java)
            bindFactory(OtherServiceFactory::class.java)
                .to(OtherServiceInterface::class.java)
                .`in`(Singleton::class.java)
        }
    }
}
```
Ensure that a class implementing `org.radarbase.jersey.auth.ProjectService` is added to the binder. This is done automatically if you configure a `MPConfig.clientId` and `MPConfig.clientSecret`. Then the projects will be fetched from ManagementPortal.

The following variables will be fetched from environment variables if set:\
`MANAGEMENT_PORTAL_CLIENT_ID` sets `AuthConfig.managementPortal.clientId`\
`MANAGEMENT_PORTAL_CLIENT_SECRET` sets `AuthConfig.managementPortal.clientSecret`\
`AUTH_KEYSTORE_PASSWORD` sets `AuthConfig.jwtKeystorePassword`\
`DATABASE_URL` sets `DatabaseConfig.url`\
`DATABASE_USER` sets `DatabaseConfig.user`\
`DATABASE_PASSWORD` sets `DatabaseConfig.password`

This factory can then be specified in your main method, by adding it to your `MyConfigClass` definition:
```kotlin
fun main(args: Array<String>) {
    val config: MyConfigClass = ConfigLoader.loadConfig("my-config-name.yml", args)
    val resources = ConfigLoader.loadResources(config.resourceConfig, config)
    val server = GrizzlyServer(config.baseUri, resources, config.isJmxEnabled)
    // Listen until JVM shutdown
    server.listen()
}
```

### Error handling

Errors are handled by adding the `ConfigLoader.Enhancers.httpException` enhancer. This adds error handling for `org.radarbase.jersey.exception.HttpApplicationException` exceptions and its subclasses can be used and extended to serve detailed error messages with customized logging and HTML templating. They can be thrown from any resource.

To serve custom HTML error messages for error codes 400 to 599, add a Mustache template to the classpath in directory `org/radarbase/jersey/exception/mapper/<code>.html`. You can use special cases `4xx.html` and `5xx.html` as a catch-all template. The templates can use variables `status` for the HTTP status code, `code` for shorthand code for the specific error, and an optional `detailedMessage` for a human-readable message.

Any other uncaught exceptions can be handled by adding the `ConfigLoader.Enhancers.generalException`.

### Logging

To enable logging with radar-jersey, please set the following configurations. For new projects, the default should be Log4j 2. A configuration file is included in the classpath. First include the following dependencies:

```kotlin
dependencies {
    // To enable logging either use log4j
    val log4j2Version: String by project
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:$log4j2Version")
    runtimeOnly("org.apache.logging.log4j:log4j-core:$log4j2Version")
    runtimeOnly("org.apache.logging.log4j:log4j-jul:$log4j2Version")
}
```

Then before any other command is made, set:
```kotlin
// Initialize logging with log4j2
System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager")
```
Execute this statement before ANY logging or logging initialization code has been called, for example in the `init` of a companion object of the main class. Alternatively, set it as a Java system property in the command line, i.e. `-Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager`. 

If Logback is used instead, import the following dependencies to gradle:

```kotlin
dependencies {
    val logbackVersion: String by project
    runtimeOnly("ch.qos.logback:logback-classic:$logbackVersion")
    val slf4jVersion: String by project
    implementation("org.slf4j:jul-to-slf4j:$slf4jVersion")
}
```

Then before any logging code has been called, set:
```kotlin
SLF4JBridgeHandler.removeHandlersForRootLogger()
SLF4JBridgeHandler.install()
```

### Health

A `/health` endpoint can be added with `ConfigLoader.Enhancers.health`. It has the response structure `{"status":"UP","myhealth:{"status":"UP","numberOfSomething":5}}`. It reports main status `DOWN` if any metric status is `DOWN`, and `UP` otherwise. A health metric can be added by binding a `HealthService.Metric` named to your metric name, e.g.:
```kotlin
bind(MyMetric::class.java)
    .named("mymetric")
    .to(HealthService.Metric::class.java)
```
The implementation may optionally return health status `UP` or `DOWN` and may in addition expose custom metrics that should be serializable by Jackson. The status is not automatically shown in the response. It is only shown if it is added as part of the `metrics` property implementation of `HealthService.Metrics`. 

### Caching

Client side caching is enabled by the `Filters.cache` filter. When this is enabled, resource methods and classes can be annotated with a `org.radarbase.jersey.cache.Cache` or `NoCache` annotation. The fields of this annotation correspond to the [`Cache-Control` headers](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Cache-Control).

### OpenAPI / Swagger

To automatically create a OpenAPI / Swagger endpoint for your API, add the `Enhancers.openapi` resource enhancer. Provide it with a general description of your API as specified by an `OpenAPI` object.
