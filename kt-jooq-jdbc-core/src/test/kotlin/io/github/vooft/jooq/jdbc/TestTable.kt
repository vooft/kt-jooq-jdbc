package io.github.vooft.jooq.jdbc

import org.jooq.DSLContext
import org.jooq.impl.DSL
import java.util.UUID

object TestTable {
    private val tableName = UUID.randomUUID().toString().replace("-", "")
    val table = DSL.table(DSL.name(tableName))
    val column = DSL.field(DSL.name("id"), UUID::class.java)

    fun DSLContext.createTable() {
        if (meta().tables.any { it.name == tableName }) {
            return
        }

        createTable(table).column(column).execute()
    }
}
