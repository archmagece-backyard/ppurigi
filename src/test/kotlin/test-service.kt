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
        val roomId = "R_ABC"
        val ownerUserId = 1L
        val scatterResponse = ppooService.scatter(roomId, ownerUserId, 10000, 3)
        val token = scatterResponse.token

        val hunterUserId = 2L
        ppooService.gather(roomId, hunterUserId, token)
        ppooService.inspection(roomId, ownerUserId, token)
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
        val scatterResponse = ppooService.scatter(roomId, ownerUserId, 10000, 3)
        val token = scatterResponse.token

        val hunterUserId = 3L
        val amount = ppooService.gather(roomId, hunterUserId, token)
        assertNotNull(amount)
    }

    @Test
    fun `ppurigi service - 상품을 3개 생성한걸 4번 받아감`() {
        val roomId = "AA1"
        val ownerUserId = 1L
        val scatterResponse = ppooService.scatter(roomId, ownerUserId, 10000, 3)
        val token = scatterResponse.token

        val hunterUserId2 = 2L
        val gatherResponse2 = ppooService.gather(roomId, hunterUserId2, token)
        assertNotNull(gatherResponse2)
        val hunterUserId3 = 3L
        val gatherResponse3 = ppooService.gather(roomId, hunterUserId3, token)
        assertNotNull(gatherResponse3)
        val hunterUserId4 = 4L
        val gatherResponse4 = ppooService.gather(roomId, hunterUserId4, token)
        assertNotNull(gatherResponse4)
        assertFailsWith<PpooStatusException> {
            val hunterUserId5 = 5L
            val gatherResponse5 = ppooService.gather(roomId, hunterUserId5, token)
            assertNotNull(gatherResponse5)
        }
//        val hunterUserId6 = 6L
//        val gatherResponse6 = ppooService.gather(roomId, hunterUserId6, token)
//        assertNotNull(gatherResponse6)
        val inspectionResponse = ppooService.inspection(roomId, ownerUserId, token)
        assertNotNull(inspectionResponse)
    }

    @Test
    fun `ppurigi service - 한 방에서 뿌린걸 다른방에서 접근`() {
        val roomId = "AA1"
        val ownerUserId = 2L
        val scatterResponse = ppooService.scatter(roomId, ownerUserId, 10000, 3)
        val token = scatterResponse.token

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
        val scatterResponse = ppooService.scatter(roomId, ownerUserId, 10000, 3)
        val token = scatterResponse.token

        assertFailsWith<PpooStatusException> {
            ppooService.gather(roomId, ownerUserId, token)
        }
    }

    @Test
    fun `ppurigi service - 한 사용자가 두번 받으러 접근`() {
        val roomId = "AA1"
        val ownerUserId = 2L
        val scatterResponse = ppooService.scatter(roomId, ownerUserId, 10000, 3)
        val token = scatterResponse.token

        val userId = 3L
        val gatherResponse1 = ppooService.gather(roomId, userId, token)
        assertFailsWith<PpooStatusException> {
            val gatherResponse2 = ppooService.gather(roomId, userId, token)
        }
    }

    @Test
    fun `ppurigi service - 10분 지난 뿌리기를 받으려고 요청`() {
        val roomId = "AA1"
        val ownerUserId = 2L
        val scatterResponse = ppooService.scatter(roomId, ownerUserId, 10000, 3)
        val token = scatterResponse.token

        transaction {
            PpooEventTable.update({ PpooEventTable.roomId.eq(roomId) and PpooEventTable.token.eq(token)}) {
                it[PpooEventTable.createdAt] = DateTime.now(DateTimeZone.UTC).minusMinutes(8)
            }
        }

        val userId = 3L
        val gatherResponse1 = ppooService.gather(roomId, userId, token)

        transaction {
            PpooEventTable.update({ PpooEventTable.roomId.eq(roomId) and PpooEventTable.token.eq(token)}) {
                it[PpooEventTable.createdAt] = DateTime.now(DateTimeZone.UTC).minusMinutes(10)
            }
        }
        assertFailsWith<PpooStatusException> {
            val gatherResponse2 = ppooService.gather(roomId, userId, token)
        }
    }

    @Test
    fun `ppurigi service - AA1 방에서 owner가 뿌린걸 같은 방에서 owner가 확인`() {
        val roomId = "AA1"
        val ownerUserId = 1L
        val scatterResponse = ppooService.scatter(roomId, ownerUserId, 10000, 3)
        val token = scatterResponse.token

        val userId = 3L
        val gatherResponse = ppooService.gather(roomId, userId, token)

        val inspectionResponse = ppooService.inspection(roomId, ownerUserId, token)
        println(inspectionResponse)
    }

    @Test
    fun `ppurigi service - AA1 방에서 owner가 뿌린걸 같은 방에서 owner가 확인 2명이 받았을 때 금액 변화 확인`() {
        val roomId = "AA1"
        val ownerUserId = 1L
        val scatterResponse = ppooService.scatter(roomId, ownerUserId, 10000, 3)
        val token = scatterResponse.token

        val userId1 = 3L
        val gatherResponse1 = ppooService.gather(roomId, userId1, token)
        val inspectionResponse1 = ppooService.inspection(roomId, ownerUserId, token)

        val userId2 = 4L
        val gatherResponse2 = ppooService.gather(roomId, userId2, token)
        val inspectionResponse2 = ppooService.inspection(roomId, ownerUserId, token)

        println(inspectionResponse1)
        println(inspectionResponse2)
        assert(inspectionResponse1.gathers.size + 1 == inspectionResponse2.gathers.size)
        assert(inspectionResponse1.sumAmountGathered < inspectionResponse2.sumAmountGathered)
    }

    @Test
    fun `ppurigi service - AA1 방에서 owner가 뿌린걸 같은 방에서 user4 확인 2명이 받았을 때 금액 변화 확인`() {
        val roomId = "AA1"
        val ownerUserId = 1L
        val scatterResponse = ppooService.scatter(roomId, ownerUserId, 10000, 3)
        val token = scatterResponse.token

        val userId1 = 3L
        val gatherResponse1 = ppooService.gather(roomId, userId1, token)
        val inspectionResponse1 = ppooService.inspection(roomId, ownerUserId, token)

        assertFailsWith<PpooStatusException>{
            val userId2 = 4L
            val gatherResponse2 = ppooService.gather(roomId, userId2, token)
            val inspectionResponse2 = ppooService.inspection(roomId, userId2, token)
        }
    }

    @Test
    fun `ppurigi service - AA1 방에서 owner가 뿌린걸 같은 방에서 다른사람(user4) 확인 2명이 받았을 때`() {
        val roomId = "AA1"
        val ownerUserId = 1L
        val scatterResponse = ppooService.scatter(roomId, ownerUserId, 10000, 3)
        val token = scatterResponse.token

        val userId1 = 3L
        val gatherResponse1 = ppooService.gather(roomId, userId1, token)
        val inspectionResponse1 = ppooService.inspection(roomId, ownerUserId, token)

        assertFailsWith<PpooStatusException>{
            val userId2 = 4L
            val gatherResponse2 = ppooService.gather(roomId, userId2, token)
            val inspectionResponse2 = ppooService.inspection(roomId, userId2, token)
        }
    }

    @Test
    fun `ppurigi service - AA1 방에서 owner가 조회기간7일 이후에 조회`() {
        val roomId = "AA1"
        val ownerUserId = 1L
        val scatterResponse = ppooService.scatter(roomId, ownerUserId, 10000, 3)
        val token = scatterResponse.token

        val userId1 = 3L
        val gatherResponse1 = ppooService.gather(roomId, userId1, token)
        val inspectionResponse1 = ppooService.inspection(roomId, ownerUserId, token)

        transaction {
            PpooEventTable.update({ PpooEventTable.roomId.eq(roomId) and PpooEventTable.token.eq(token)}) {
                it[PpooEventTable.createdAt] = DateTime.now(DateTimeZone.UTC).minusDays(7)
            }
        }
        assertFailsWith<PpooStatusException> {
            val inspectionResponse2 = ppooService.inspection(roomId, ownerUserId, token)
        }
    }

}
