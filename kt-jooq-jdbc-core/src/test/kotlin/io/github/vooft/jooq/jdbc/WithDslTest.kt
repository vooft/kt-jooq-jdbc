package io.github.vooft.jooq.jdbc

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.blockhound.BlockHound

class WithDslTest : IntegrationTest() {
    private lateinit var dataSource: HikariDataSource

    @BeforeEach
    fun setup() {
        BlockHound.install()

        dataSource =
            HikariDataSource(
                HikariConfig().apply {
                    jdbcUrl = psql.jdbcUrl
                    username = psql.username
                    password = psql.password
                },
            )
    }

    @AfterEach
    fun destroy() {
        dataSource.close()
    }

    @Test
    fun `should execute block`(): Unit =
        runBlocking {
            val withDsl = WithDsl(DSL.using(dataSource, SQLDialect.POSTGRES))
            val one =
                withDsl {
                    selectOne().execute()
                }

            one shouldBe 1
        }
}
