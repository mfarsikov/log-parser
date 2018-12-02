package mfarsikov.parser

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.io.File


fun readSettings(args: Array<String>): Settings {
    val argToValue = args.map { it.substringAfter("--").split("=") }
            .map { (key, value) -> key to value }
            .toMap()

    val dbProps = File("db.properties").also { if (!it.exists()) throw Exception("Add db.properties file with `password=my_password` to current dir") }
            .readLines()
            .map { it.split("=") }
            .map { (key, value) -> key to value }
            .toMap()

    val startDate = argToValue["startDate"]
            ?.let { DateTime.parse(it, DateTimeFormat.forPattern("yyyy-MM-dd.HH:mm:ss")) }
            ?: DateTime(2010, 1, 1, 0, 0, 0)

    val duration = argToValue["duration"].takeIf { it == "daily" } ?: "hourly"

    val endDate = when (duration) {
        "hourly" -> startDate.plusHours(1)
        "daily" -> startDate.plusDays(1)
        else -> throw Exception("invalid duration $duration")
    }

    return Settings(startDate = startDate,
                    endDate = endDate,
                    duration = duration,
                    threshold = argToValue["threshold"]?.toInt() ?: 0,
                    filePath = argToValue["filePath"] ?: "access.log",
                    insertToLogTable = argToValue["insertToLogTable"] ?: "none",
                    dbSettings = DbSettings(
                            host = dbProps["host"] ?: "localhost",
                            port = dbProps["port"]?.toInt() ?: 3306,
                            dbName = dbProps["dbName"] ?: "logs",
                            user = dbProps["user"] ?: "root",
                            password = dbProps["password"]
                                    ?: throw Exception("DB password not found in db.properties")
                    ))
}

class Settings(
        val startDate: DateTime,
        val endDate: DateTime,
        val duration: String,
        val threshold: Int,
        val filePath: String,
        val insertToLogTable: String,
        val dbSettings: DbSettings
)

class DbSettings(
        val host: String,
        val port: Int,
        val dbName: String,
        val user: String,
        val password: String
)