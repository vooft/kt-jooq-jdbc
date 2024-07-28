package io.github.vooft.jooq.jdbc

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.jupiter.api.Test

class WithDslTest : IntegrationTest() {
    @Test
    fun `should execute block`() {
        HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = psql.jdbcUrl
                username = psql.username
                password = psql.password
            },
        ).use { dataSource ->
            val withDsl = WithDsl(DSL.using(dataSource, SQLDialect.POSTGRES))
            runBlocking {
                val one =
                    withDsl {
                        selectOne().execute()
                    }

                one shouldBe 1
            }
        }
    }
}
