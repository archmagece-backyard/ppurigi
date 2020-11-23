import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.tests.utils.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class TestService {

    val ppooService = PpooService()


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
        val db = Database.connect(ds).apply {
            useNestedTransactions = true
        }
        transaction {
            SchemaUtils.drop(PpooEventTable, PpooPrizeTable, PpooPrizewinnerTable)
            SchemaUtils.create(PpooEventTable, PpooPrizeTable, PpooPrizewinnerTable)
        }
    }

    @Test
    fun `ppurigi service - single success case`() {
        val pRoomId = "R_ABC"
        val pUserId = 1L
        val rToken = ppooService.scatter(pRoomId, pUserId, 10000, 3)
        ppooService.gather(pRoomId, pUserId, rToken)
        ppooService.inspection(pRoomId, pUserId, rToken)
    }

    @Test
    fun `ppurigi service - 뿌리기 여러개 생성`() {
        var roomId: String = "AA1"
        var userId: Long = 1L
        val rToken1 = ppooService.scatter(roomId, userId, 10000, 3)
        roomId = "AA2"
        userId = 1L
        val rToken2 = ppooService.scatter(roomId, userId, 10000, 3)
        roomId = "AA3"
        userId = 1L
        val rToken3 = ppooService.scatter(roomId, userId, 10000, 3)
        roomId = "AA1"
        userId = 2L
        val rToken4 = ppooService.scatter(roomId, userId, 10000, 3)
        roomId = "AA1"
        userId = 2L
        val rToken5 = ppooService.scatter(roomId, userId, 10000, 3)
        roomId = "AA1"
        userId = 2L
        val rToken6 = ppooService.scatter(roomId, userId, 10000, 3)
    }

    @Test
    fun `ppurigi service - 정상케이스 - owner가 생선 같은방의 hunter가 1회 받아감`() {
        val roomId = "AA1"
        val ownerUserId = 2L
        val token = ppooService.scatter(roomId, ownerUserId, 10000, 3)

        val hunterUserId = 3L
        val amount = ppooService.gather(roomId, hunterUserId, token)
        assertNotNull(amount)
    }

    @Test
    fun `ppurigi service - 한 방에서 뿌린걸 다른방에서 접근`() {
        val roomId = "AA1"
        val ownerUserId = 2L
        val token = ppooService.scatter(roomId, ownerUserId, 10000, 3)

        val userId = 3L
        ppooService.gather(roomId, userId, token)
        assertFailsWith<PpooStatusException> {
            ppooService.gather("AA3", userId, token)
        }
    }

    @Test
    fun `ppurigi service - 이벤트 Owner가 이벤트 참여`() {
        val roomId = "AA1"
        val ownerUserId = 2L
        val token = ppooService.scatter(roomId, ownerUserId, 10000, 3)

        assertFailsWith<PpooStatusException> {
            ppooService.gather(roomId, ownerUserId, token)
        }
    }

    @Test
    fun `ppurigi service - 한 사용자가 두번 받으러 접근`() {
        val roomId = "AA1"
        val ownerUserId = 2L
        val token = ppooService.scatter(roomId, ownerUserId, 10000, 3)

        val userId = 3L
        val amount1 = ppooService.gather(roomId, userId, token)
        assertFailsWith<PpooStatusException> {
            val amount2 = ppooService.gather(roomId, userId, token)
        }
    }

    @Test
    fun `ppurigi service - 10분 지난 뿌리기를 받으려고 요청`() {
        val roomId = "AA1"
        val ownerUserId = 2L
        val token = ppooService.scatter(roomId, ownerUserId, 10000, 3)

        transaction {
            PpooEventTable.update({ PpooEventTable.roomId.eq(roomId) and PpooEventTable.token.eq(token)}) {
                it[PpooEventTable.createdAt] = DateTime.now(DateTimeZone.UTC).minusMinutes(8)
            }
        }

        val userId = 3L
        val amount1 = ppooService.gather(roomId, userId, token)

        transaction {
            PpooEventTable.update({ PpooEventTable.roomId.eq(roomId) and PpooEventTable.token.eq(token)}) {
                it[PpooEventTable.createdAt] = DateTime.now(DateTimeZone.UTC).minusMinutes(10)
            }
        }
        assertFailsWith<PpooStatusException> {
            val amount2 = ppooService.gather(roomId, userId, token)
        }
    }

    @Test
    fun `ppurigi service - AAA 방에서 뿌린걸 AAA방에서 접근하고 AAA방에서 만든사람이 확인`() {

    }

    @Test
    fun `ppurigi service - AAA 방에서 뿌린걸 AAA방에서 접근하고 AAA방에서 다른 사람이 확인`() {

    }
}
