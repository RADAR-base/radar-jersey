# radar-jersey

Library to facilitate using with a Jersey-based REST API. This includes OAuth 2.0 integration, exception handling and resource configuration.

## Usage

Add this library to your project using the following Gradle configuration:
```gradle
repositories {
    mavenCentral()
}

dependencies {
    api("org.radarbase:radar-jersey:0.8.1")
}
```

Any path or resource that should be authenticated against the ManagementPortal, should be annotated with `@Authenticated`. Specific authorization can be checked by adding a `@NeedsPermission` annotation. An `Auth` object can be injected to get app-specific information. For reliable injection, constructor or method injection, not class parameter injection. Examples:

```kotlin
@Path("/projects")
@Authenticated
class Users(
    @Context projectService: MyProjectService
) {
    @GET
    @NeedsPermission(PROJECT, READ)
    fun getProjects(@Context auth: Auth): List<Project> {
        return projectService.read()
            .filter { auth.token.hasPermissionOnProject(PROJECT_READ, it.name) }
    }

    @POST
    @Path("/{projectId}")
    @NeedsPermission(PROJECT, UPDATE, "projectId")
    fun updateProject(@PathParam("projectId") projectId: String, project: Project) {
        return projectService.update(projectId, project)
    }

    @GET
    @Path("/{projectId}/users/{userId}")
    @NeedsPermission(SUBJECT, READ, "projectId", "userId")
    fun getUsers(@PathParam("projectId") projectId: String, @PathParam("userId") userId: String) {
        return projectService.readUser(projectId, userId)
    }
}
```

These APIs are activated by adding an `EnhancerFactory` implementation to your resource definition:
```kotlin
class MyEnhancerFactory(private val config: MyConfigClass): EnhancerFactory {
    override fun createEnhancers(): List<JerseyResourceEnhancer> {
        val authConfig = AuthConfig(
            managementPortal = MPConfig(
                url = "http://...",
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
        override val classes: Array<Class<*>> = arrayOf(
            Filters.logResponse,
            Filters.cors,
            Filters.cache,
        )

        override val packages = arrayOf(
            "com.example.app.resources",
        )

        override fun AbstractBinder.enhance() {
            bind(config)
                .to(MyConfigClass::class.java)
            bind(MyService::class.java)
                .to(MyService::class.java)
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

To serve custom HTML error messages for error codes 400 to 599, add a Mustache template to the classpath in directory `org/radarbase/jersey/exception/mapper/<code>.html`. You can use special cases `4xx.html` and `5xx.html` as a catch-all template. The templates can use variables `status` for the HTTP status code, `code` for short-hand code for the specific error, and an optional `detailedMessage` for a human-readable message.

Any other uncaught exceptions can be handled by adding the `ConfigLoader.Enhancers.generalException`.

### Logging

To enable logging with radar-jersey, please set the following configurations. For new projects, the default should be Log4j 2. A configuration file is included in the classpath. First include the following dependencies:

```kotlin
dependencies {
    // To enable logging either use log4j
    val log4j2Version: String by project
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:$log4j2Version")
    runtimeOnly("org.apache.logging.log4j:log4j-api:$log4j2Version")
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
    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")
    implementation("org.slf4j:jul-to-slf4j:1.7.32")
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
