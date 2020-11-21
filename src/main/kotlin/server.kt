import com.codahale.metrics.Slf4jReporter
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.metrics.dropwizard.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.jetbrains.exposed.sql.Database
import org.slf4j.event.Level
import java.lang.reflect.Modifier
import java.text.DateFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun initConfig() {
    ConfigFactory.defaultApplication()
}

fun initDB() {
    val dbType = ConfigFactory.load().getString("db_type")
    val config = ConfigFactory.load().getConfig(dbType)
    val properties = Properties()
    config.entrySet().forEach { e -> properties.setProperty(e.key, config.getString(e.key)) }
    val hikariConfig = HikariConfig(properties)
    val ds = HikariDataSource(hikariConfig)
    Database.connect(ds)
}

fun dbMigrate() {
    DBMigration.migrate()
}

fun Application.module() {
    install(Compression)
    install(DefaultHeaders)
    install(CallLogging) {
        filter { call -> !call.request.path().startsWith("/employee-api/health") }
        level = Level.TRACE
        mdc("executionId") {
            UUID.randomUUID().toString()
        }
    }
    install(ContentNegotiation) {
        gson {
            setDateFormat(DateFormat.LONG)
            setPrettyPrinting()
            excludeFieldsWithModifiers(Modifier.TRANSIENT)
        }
    }

//    install(DropwizardMetrics) {
//        JmxReporter.forRegistry(registry)
//            .convertRatesTo(TimeUnit.SECONDS)
//            .convertDurationsTo(TimeUnit.MILLISECONDS)
//            .build()
//            .start()
//    }
    install(DropwizardMetrics) {
        Slf4jReporter.forRegistry(registry)
            .outputTo(log)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build()
            .start(10, TimeUnit.SECONDS)
    }

    initConfig()
//    dbMigrate()
    initDB()

    install(Routing) {
        ppurigi()
    }
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "127.0.0.1", module = Application::module).start(wait = true)
}