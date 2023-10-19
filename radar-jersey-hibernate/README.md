# radar-jersey-hibernate

Database extensions for radar-jersey. Includes support for [Hibernate](https://hibernate.org) and [Liquibase](https://www.liquibase.org).

# Usage

Add this library to your project using the following Gradle configuration:
```kotlin
dependencies {
    implementation("org.radarbase:radar-jersey-hibernate:<version>")
}
```

To make full use if the module, let any database-using classes extend `HibernateRepository`. Any database operations must be wrapped in a `transact { doSomething() }` or `createTransaction { doSomething() }.use { result -> doSomething result }`. Both closures have EntityManager as `this` object, meaning that `createQuery` and derivatives should be called without referencing an additional `EntityManager`.

By default, Liquibase manages database versioning. It can be disabled by setting `DatabaseConfig.liquibase.enable` to `false`. If enabled, liquibase expects the master changelog file to reside in resources at `DatabaseConfig.liquibase.changelogs` (default `db/changelog/changes/db.changelog-master.xml`.)

Example repository code:

```kotlin
class ProjectRepositoryImpl(
    @Context em: Provider<EntityManager>,
    @Context asyncService: AsyncCoroutineService,
    ): ProjectRepository, HibernateRepository(em, asyncService) {
    suspend fun list(): List<ProjectDao> = transact {
        createQuery("SELECT p FROM Project p", ProjectDao::class.java)
                .resultList
    }
}
```
