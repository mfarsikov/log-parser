package mfarsikov.parser

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.transactions.transaction


object BlockedIpTable : Table("blocked_ip") {
    val ip: Column<String> = varchar("ip", 255)
    val reason: Column<String> = varchar("reason", 255)

    fun batchInsert(blockedIps: List<BlockedIp>) {

        transaction {
            BlockedIpTable.batchInsert(blockedIps) {
                this[ip] = it.ip
                this[reason] = it.reason
            }
        }
        println("Inserted ${blockedIps.size} records to 'blocked_ip' table ")
    }
}

data class BlockedIp(
        val ip: String,
        val reason: String
)
