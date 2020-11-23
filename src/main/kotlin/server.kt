import com.codahale.metrics.Slf4jReporter
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.metrics.dropwizard.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.Database
import org.slf4j.event.Level
import java.lang.reflect.Modifier
import java.text.DateFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun initConfig() = ConfigFactory.defaultApplication() ?: throw NullPointerException("init error on server.kt")

fun initDB(baseConfig: Config) {
    ConfigFactory.load().withFallback(baseConfig).apply {
        val dbType = getString("db_type")
        val config = getConfig(dbType)
        val hikariConfig = HikariConfig(Properties().apply {
            config.entrySet().forEach { e -> setProperty(e.key, config.getString(e.key)) }
        })
        val ds = HikariDataSource(hikariConfig)
        Database.connect(ds)
    }
}

fun dbMigrate() {
    DBMigration.migrate()
}

fun Application.module() {
    install(Compression)
    install(DefaultHeaders)
    install(CallLogging) {
        filter { call -> !call.request.path().startsWith(Constants.URI_HEALTH) }
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
    install(DropwizardMetrics) {
        Slf4jReporter.forRegistry(registry)
            .outputTo(log)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build()
            .start(10, TimeUnit.SECONDS)
    }

    initDB(initConfig())
//    dbMigrate()

    val ppurigiService = PpooService()

    install(Routing) {
        ppurigi(ppurigiService)
    }
    install(StatusPages) {
        exception<IllegalArgumentException> {
            call.respond(
                HttpStatusCode.BadRequest,
                ResponseWrapper(
                    code = PpooStatusCode.FAIL.code,
                    message = PpooStatusCode.FAIL.message,
                    data = ""
                )
            )
//            call.respond(HttpStatusCode.BadRequest) {
//                ResponseWrapper(
//                    code = PpooStatusCode.FAIL.code,
//                    message = PpooStatusCode.FAIL.message,
//                    data = ""
//                )
//            }
        }
        exception<PpooStatusException> { cause ->
            call.respond(
                HttpStatusCode.OK,
                ResponseWrapper(
                    code = cause.statusCode.code,
                    message = cause.statusCode.message,
                    data = ""
                )
            )
//            call.respond(HttpStatusCode.OK) {
//                ResponseWrapper(
//                    code = cause.statusCode.code,
//                    message = cause.statusCode.message,
//                    data = ""
//                )
//            }
        }
    }
}


fun main() {
    System.setProperty("testing", "false")
//    System.setProperty("db_type", "maria")
    embeddedServer(Netty, port = 8080, host = "127.0.0.1", module = Application::module).start(wait = true)
}