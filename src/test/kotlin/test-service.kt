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
            SchemaUtils.create (Scatter, Treasure, Hunter)
        }
    }

    @Test
    fun `ppurigi service test`() {
        val pRoomId = 1L
        val pUserId = 1L
        val ppurigiService = PpurigiService()
        val rToken = ppurigiService.scatter(pRoomId, pUserId, 10000, 3)
        ppurigiService.gather(pRoomId, pUserId, rToken)
        ppurigiService.inspect(pRoomId, pUserId, rToken)
    }

}
