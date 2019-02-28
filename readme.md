# Log parser





### Database

To run database in docker:

    docker run --name mysql -e MYSQL_ROOT_PASSWORD=my-secret-pw -e MYSQL_DATABASE=logs -p 3306:3306 -d mysql:8

App uses `db.properties` file for configuration

Tables `log` and `blocked_ip` are created automatically during application run

### Build

`./gradlew shadowJar`

This command creates so called 'fat jar' with all dependencies included: `build/libs/parser-0.0.1-SNAPSHOT-all.jar`

For brevity below this file is called just `parser.jar`

### Run 

##### Arguments and defaults
Application could be run as:
    
    java -jar parser.jar
    
With these defaults:
```
--startDate=2010-01-01.00:00:00
--duration=hourly
--threshold=0
--filePath=access.log
--insertToLogTable=none
```

##### IPs exceeded threshold

    java -jar parser.jar --startDate=2017-01-01.15:00:00 --duration=hourly --threshold=200

This command:
- Saves IPs exceeded threshold to `blocked_ip` table
- Prints IPs exceeded threshold to console

##### Save ALL parsed log records  to  DB (--insertToLogTable=all)

    java -jar parser.jar --startDate=2017-01-01.15:00:00 --duration=hourly --threshold=200 --insertToLogTable=all

This command:
- Inserts **all** records from log file to `log` table
- Saves IPs exceeded threshold to `blocked_ip` table
- Prints IPs exceeded threshold to console

##### Save log records, which exceeded threshold (--insertToLogTable=exceededThreshold)

    java -jar parser.jar --startDate=2017-01-01.15:00:00 --duration=hourly --threshold=200 --insertToLogTable=exceededThreshold

This command:
- Inserts records **which exceeded threshold**, from log file to `log` table
- Saves IPs exceeded threshold to `blocked_ip` table
- Prints IPs exceeded threshold to console

#### SQL

If `insertToLogTable` flag was `exceededThreshold` or `all` then next SQL query could be ran  against `log` table:

```sql
SELECT DISTINCT ip
FROM log
WHERE timestamp BETWEEN '2017-01-01T15:00:00' AND '2017-01-01T16:00:00'
GROUP BY ip
HAVING count(*) > 100;
```
