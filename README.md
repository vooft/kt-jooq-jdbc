![Build and test](https://github.com/vooft/kt-jooq-jdbc/actions/workflows/build.yml/badge.svg?branch=main)
![Releases](https://img.shields.io/github/v/release/vooft/kt-jooq-jdbc)
![Maven Central](https://img.shields.io/maven-central/v/io.github.vooft/kt-jooq-jdbc-core)
![License](https://img.shields.io/github/license/vooft/kt-jooq-jdbc)

### kt-jooq-jdbc
Kotlin Coroutines wrapper around jOOQ JDBC using virtual threads.

### Usage
#### Gradle
```kotlin
    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("io.github.vooft:kt-jooq-jdbc-core:<version>")
    }
```

#### Non-transactional queries
```kotlin
    val dataSource = createMyDataSource()

    val withDsl = WithDsl(dataSource)
    return withDsl {
        selectFrom(MY_TABLE)
            .where(MY_TABLE.ID.eq(1))
            .fetchOne()
    }
```

#### Transactional queries
```kotlin
    val dataSource = createMyDataSource()

    val withTransactionalDsl = WithTransactionalDsl(dataSource)
    return withTransactionalDsl {
        insertInto(USERS).set(USERS.USERNAME, "test").execute()
        insertInto(ROLES).set(ROLES.USERNAME, "test").set(ROLES.ROLE, "admin").execute()
    }
```

#### Singleton bean
It is designed in mind to be a singleton bean, for example in Spring it could be used like this:

```kotlin
@Configuration
class JooqConfiguration {
    @Bean
    fun withDsl(dataSource: DataSource): WithDsl {
        return WithDsl(dataSource)
    }

    @Bean
    fun withTransactionalDsl(dataSource: DataSource): WithTransactionalDsl {
        return WithTransactionalDsl(dataSource)
    }
}

@Service
class UserService(private val withDsl: WithDsl, private val withTransactionalDsl: WithTransactionalDsl) {
    suspend fun findUserById(id: Int): User? {
        return withDsl {
            selectFrom(USERS)
                .where(USERS.ID.eq(id))
                .fetchOne()
                ?.into(User::class.java)
        }
    }

    suspend fun createUser(username: String, role: String) {
        withTransactionalDsl {
            insertInto(USERS).set(USERS.USERNAME, username).execute()
            insertInto(ROLES).set(ROLES.USERNAME, username).set(ROLES.ROLE, role).execute()
        }
    }
}

```

### Custom dispatcher
By default, this library uses a new virtual thread executor as a dispatcher. 
If you want to use a custom dispatcher, you can pass it as a parameter to the constructor.

```kotlin
    val withDsl = WithDsl(dataSource, Dispatchers.IO)
```
