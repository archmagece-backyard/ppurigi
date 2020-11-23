import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test

class TestService {

    @BeforeTest
    fun before(){
        System.setProperty("testing", "true")
        System.setProperty("db_type", "h2")
        val dbType = ConfigFactory.load().getString("db_type")
        val config = ConfigFactory.load().getConfig(dbType)
        val properties = Properties()
        config.entrySet().forEach { e -> properties.setProperty(e.key, config.getString(e.key)) }
        val hikariConfig = HikariConfig(properties)
        val ds = HikariDataSource(hikariConfig)
        Database.connect(ds).apply {
            useNestedTransactions = true
        }
        transaction {
            SchemaUtils.create (PpooEventTable, PpooPrizeTable, PpooPrizewinnerTable)
        }
    }

    @Test
    fun `ppurigi service - `() {
        val pRoomId = "R_ABC"
        val pUserId = 1L
        val ppooService = PpooService()
        val rToken = ppooService.scatter(pRoomId, pUserId, 10000, 3)
        ppooService.gather(pRoomId, pUserId, rToken)
        ppooService.inspection(pRoomId, pUserId, rToken)
    }

}
