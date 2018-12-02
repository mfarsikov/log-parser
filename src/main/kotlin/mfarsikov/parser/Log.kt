package mfarsikov.parser

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime


object LogTable : Table("log") {
    val timestamp: Column<DateTime> = datetime("timestamp")
    val ip: Column<String> = varchar("ip", 255)
    val method: Column<String> = varchar("method", 255)
    val status: Column<String> = varchar("status", 255)
    val message: Column<String> = varchar("message", 255)

    fun batchInsert(logs: List<Log>) {

        transaction {

            LogTable.batchInsert(logs) {
                this[timestamp] = it.timestamp
                this[ip] = it.ip
                this[method] = it.method
                this[status] = it.status
                this[message] = it.message
            }
        }
        println("Inserted ${logs.size} log records to 'log' table ")
    }
}

data class Log(
        val timestamp: DateTime,
        val ip: String,
        val method: String,
        val status: String,
        val message: String
)