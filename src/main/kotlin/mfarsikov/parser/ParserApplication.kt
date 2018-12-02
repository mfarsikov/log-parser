package mfarsikov.parser


import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.io.File


fun main(args: Array<String>) {

    val settings = readSettings(args)

    initDb(settings.dbSettings)

    val allLogs = File(settings.filePath).readLines()
            .map { parseLogLine(it) }

    println("parsed ${allLogs.size} records from ${settings.filePath}")

    val filteredLogs = allLogs
            .filter { settings.startDate <= it.timestamp && it.timestamp < settings.endDate }
            .groupBy { it.ip }
            .filter { (_, logs) -> logs.size > settings.threshold }
            .flatMap { it.value }

    val blockedIps = filteredLogs.map { it.ip }
            .distinct()
            .map {
                BlockedIp(ip = it,
                          reason = "Exceeded ${settings.threshold} requests between ${settings.startDate} and ${settings.endDate}")
            }

    if (blockedIps.isNotEmpty()) {
        println("These IPs exceeded ${settings.threshold} requests between ${settings.startDate} and ${settings.endDate}: $blockedIps")
        BlockedIpTable.batchInsert(blockedIps)
    }else{
        println("There are no IPs exceeded ${settings.threshold} requests between ${settings.startDate} and ${settings.endDate}")
    }

    when (settings.insertToLogTable) {
        "all" -> {
            println("Inserting ALL parsed rows to DB... (it could take a while)")
            LogTable.batchInsert(allLogs)
        }
        "exceededThreshold" ->{
            println("Inserting log records exceeded threshold to DB...")
            LogTable.batchInsert(filteredLogs)
        }
    }

}

fun initDb(dbSettings: DbSettings) {
    DateTimeZone.setDefault(DateTimeZone.UTC)

    Database.connect(url = "jdbc:mysql://${dbSettings.host}:${dbSettings.port}/${dbSettings.dbName}",
                     user = dbSettings.user,
                     password = dbSettings.password,
                     driver = "com.mysql.cj.jdbc.Driver")

    transaction {
        SchemaUtils.create(BlockedIpTable)
        SchemaUtils.create(LogTable)
    }
}

fun parseLogLine(logLine: String): Log {

    val columns = logLine.split("|")

    return Log(
            timestamp = DateTime.parse(columns[0], DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS")),
            ip = columns[1],
            method = columns[2],
            status = columns[3],
            message = columns[4]
    )
}