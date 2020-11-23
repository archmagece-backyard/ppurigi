package http

import PpooEventTable
import PpooPrizeTable
import PpooPrizewinnerTable
import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.server.testing.*
import module
import mu.KotlinLogging
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

open class TestBase {

    protected val logger = KotlinLogging.logger { }
    protected val gson = Gson()

    protected fun ppurigiServer(callback: TestApplicationEngine.() -> Unit) {
        System.setProperty("testing", "true")
        System.setProperty("db_type", "h2")
        withTestApplication(Application::module) {
            transaction {
                SchemaUtils.dropDatabase()
                SchemaUtils.create(PpooEventTable, PpooPrizeTable, PpooPrizewinnerTable)
            }
            callback()
        }
    }
}