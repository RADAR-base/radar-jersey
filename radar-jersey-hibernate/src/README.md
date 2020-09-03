# radar-jersey-hibernate

Extension module of radar-jersey to use Hibernate in a Jersey project. Default database configuration is for PostgreSQL, but any JDBC driver compatible with Hibernate can be used. The module is activated by adding `HibernateResourceEnhancer` to your `EnhancerFactory` with a given database configuration. When this is done, `Provider<EntityManager>` can be injected via the `Context` annotation.

To make full use if the module, let any database-using classes extend `HibernateRepository`. Any database operations must be wrapped in a `transact { doSomething() }` or `createTransaction { doSomething() }.use { result -> doSomething result }`. Both closures have EntityManager as `this` object, meaning that `createQuery` and derivatives should be called without referencing an additional `EntityManager`.

By default, liquibase is used to manage database versioning. It can be disabled by setting `DatabaseConfig.liquibase.enable` to `false`. If enabled, liquibase expects the master changelog file to reside in resources at `DatabaseConfig.liquibase.changelogs` (default `db/changelog/changes/db.changelog-master.xml`.)

Example repository code:

```kotlin
class ProjectRepositoryImpl(
        @Context em: Provider<EntityManager>
): ProjectRepository, HibernateRepository(em) {
    fun list(): List<ProjectDao> = transact {
        createQuery("SELECT p FROM Project p", ProjectDao::class.java)
                .resultList
    }
}
```
