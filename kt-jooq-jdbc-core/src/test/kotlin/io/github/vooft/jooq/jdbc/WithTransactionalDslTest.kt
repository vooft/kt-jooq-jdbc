package io.github.vooft.jooq.jdbc

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import kotlinx.coroutines.runBlocking
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.blockhound.BlockHound
import java.util.UUID

class WithTransactionalDslTest : IntegrationTest() {
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
            val expected1 = UUID.randomUUID()
            val expected2 = UUID.randomUUID()

            val withTransactionalDsl = WithTransactionalDsl(DSL.using(dataSource, SQLDialect.POSTGRES))
            withTransactionalDsl {
                with(TestTable) { createTable() }
                insertInto(TestTable.table).set(TestTable.column, expected1).execute()
                insertInto(TestTable.table).set(TestTable.column, expected2).execute()
            }

            val withDsl = WithDsl(DSL.using(dataSource, SQLDialect.POSTGRES))
            val actual1 =
                withDsl {
                    selectFrom(TestTable.table).where(TestTable.column.eq(expected1)).fetchSingle().get(TestTable.column)
                }

            val actual2 =
                withDsl {
                    selectFrom(TestTable.table).where(TestTable.column.eq(expected2)).fetchSingle().get(TestTable.column)
                }

            actual1 shouldBe expected1
            actual2 shouldBe expected2
        }

    @Test
    fun `should roll back transaction if an exception is thrown`(): Unit =
        runBlocking {
            val notExpected = UUID.randomUUID()
            val message = UUID.randomUUID()

            val withTransactionalDsl = WithTransactionalDsl(DSL.using(dataSource, SQLDialect.POSTGRES))
            shouldThrow<TestException> {
                withTransactionalDsl {
                    with(TestTable) { createTable() }
                    insertInto(TestTable.table).set(TestTable.column, notExpected).execute()
                    throw TestException(message)
                }
            }.shouldHaveMessage(message.toString())

            val withDsl = WithDsl(DSL.using(dataSource, SQLDialect.POSTGRES))
            val actual =
                withDsl {
                    selectCount()
                        .from(TestTable.table)
                        .where(TestTable.column.eq(notExpected))
                        .fetchSingle()
                        .value1()
                }

            actual shouldBeExactly 0
        }
}

private class TestException(
    message: UUID,
) : Exception(message.toString())
