import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.*
import kotlin.test.*

class TestModel {

    @BeforeTest
    fun before() {
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
            SchemaUtils.drop(PpooEventTable, PpooPrizeTable, PpooPrizewinnerTable)
            SchemaUtils.create(PpooEventTable, PpooPrizeTable, PpooPrizewinnerTable)
        }
    }

    @Test
    fun `test ppurigi model insert`() {
        transaction {
            val pRoomId = "R_ABC"
            val pUserId = 1L
            val pToken = "AAA"
            val pTotalAmountOfMoney = 10000L
            val pTotalNumberOfPeople = 3L
            val scatterId1 = PpooEventTable.insertAndGetId {
                it[roomId] = pRoomId
                it[userId] = pUserId
                it[token] = pToken
                it[createdAt] = DateTime.now(DateTimeZone.UTC)
                it[totalAmountOfMoney] = pTotalAmountOfMoney
                it[totalNumberOfPeople] = pTotalNumberOfPeople
            }
            println(pTotalAmountOfMoney / pTotalNumberOfPeople)
            val amountForTreasure = pTotalAmountOfMoney / pTotalNumberOfPeople
            val treasureId1 = PpooPrizeTable.insert {
                it[event] = scatterId1.value
                it[amount] = amountForTreasure
            } get PpooPrizeTable.id
            PpooPrizewinnerTable.insert {
                it[event] = scatterId1.value
                it[prize] = treasureId1.value
                it[roomId] = pRoomId
                it[userId] = pUserId
                it[createdAt] = DateTime.now(DateTimeZone.UTC)
            }
        }
    }

    @Test
    fun `test ppurigi scatter same token duplicate - same roomId, token`() {
        transaction {
            val pTotalAmountOfMoney = 10000L
            val pTotalNumberOfPeople = 3L
            assertFailsWith(ExposedSQLException::class) {
                val pRoomId1 = "R_ABC"
                val pUserId1 = 1L
                val pToken1 = "AAA"
                val scatterId1 = PpooEventTable.insertAndGetId {
                    it[roomId] = pRoomId1
                    it[userId] = pUserId1
                    it[token] = pToken1
                    it[createdAt] = DateTime.now(DateTimeZone.UTC)
                    it[totalAmountOfMoney] = pTotalAmountOfMoney
                    it[totalNumberOfPeople] = pTotalNumberOfPeople
                }
                val scatterId2 = PpooEventTable.insertAndGetId {
                    it[roomId] = pRoomId1
                    it[userId] = pUserId1
                    it[token] = pToken1
                    it[createdAt] = DateTime.now(DateTimeZone.UTC)
                    it[totalAmountOfMoney] = pTotalAmountOfMoney
                    it[totalNumberOfPeople] = pTotalNumberOfPeople
                }
                assertNotNull(scatterId1)
                assertNotNull(scatterId2)
            }
        }
    }

    @Test
    fun `test ppurigi scatter - same token NOT duplicate - same roomId, token`() {
        transaction {
            val pTotalAmountOfMoney = 10000L
            val pTotalNumberOfPeople = 3L
            val pRoomId1 = "R_ABC"
            val pUserId1 = 1L
            val pToken1 = "AAA"
            val scatterId1 = PpooEventTable.insertAndGetId {
                it[roomId] = pRoomId1
                it[userId] = pUserId1
                it[token] = pToken1
                it[createdAt] = DateTime.now(DateTimeZone.UTC)
                it[totalAmountOfMoney] = pTotalAmountOfMoney
                it[totalNumberOfPeople] = pTotalNumberOfPeople
            }
            val pRoomId2 = "R_ABC"
            val pUserId2 = 1L
            val pToken2 = "AAB"
            val scatterId2 = PpooEventTable.insertAndGetId {
                it[roomId] = pRoomId2
                it[userId] = pUserId2
                it[token] = pToken2
                it[createdAt] = DateTime.now(DateTimeZone.UTC)
                it[totalAmountOfMoney] = pTotalAmountOfMoney
                it[totalNumberOfPeople] = pTotalNumberOfPeople
            }
            assertNotNull(scatterId1)
            assertNotNull(scatterId2)
        }
    }
}