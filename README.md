# radar-jersey

Library to facilitate using with a Jersey-based REST API. This includes OAuth 2.0 integration, exception handling and resource configuration.

# Usage

Add this library to your project using the following Gradle configuration:
```gradle
repositories {
    maven { url "https://dl.bintray.com/radar-base/org.radarbase" }
}

dependencies {
    api("org.radarbase:radar-jersey:0.2.1")
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
    override fun createEnhancers() = listOf(
            // My own resource configuration
            MyResourceEnhancer(),
            // RADAR OAuth2 enhancement
            ConfigLoader.Enhancers.radar(AuthConfig(
                    managementPortalUrl = "http://...",
                    jwtResourceName = "res_MyResource")),
            // Use ManagementPortal OAuth implementation
            ConfigLoader.Enhancers.managementPortal,
            // HttpApplicationException handling
            ConfigLoader.Enhancers.httpException,
            // General error handling (WebApplicationException and any other Exception)
            ConfigLoader.Enhancers.generalException)

    class MyResourceEnhancer: JerseyResourceEnhancer {
        override val classes: Array<Class<*>> = arrayOf(
	            ConfigLoader.Filters.logResponse,
		        ConfigLoader.Filters.cors)

        override fun AbstractBinder.enhance() {
            bind(config)
                  .to(MyConfigClass::class.java)

            bind(MyProjectService::class.java)
                  .to(ProjectService::class.java)
                  .`in`(Singleton::class.java)

            bind(MyProjectService::class.java)
                  .to(MyProjectService::class.java)
                  .`in`(Singleton::class.java)
        }
    }
}
```
Ensure that a class implementing `org.radarbase.jersey.auth.ProjectService` is added to the binder.

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

## Error handling

This package adds some error handling. Specifically, `org.radarbase.jersey.exception.HttpApplicationException` and its subclasses can be used and extended to serve detailed error messages with customized logging and HTML templating. They can be thrown from any resource.

To serve custom HTML error messages for error codes 400 to 599, add a Mustache template to the classpath in directory `org/radarbase/jersey/exception/mapper/<code>.html`. You can use special cases `4xx.html` and `5xx.html` as a catch-all template. The templates can use variables `status` for the HTTP status code, `code` for short-hand code for the specific error, and an optional `detailedMessage` for a human-readable message.
