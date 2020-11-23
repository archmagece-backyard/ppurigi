import mu.KotlinLogging
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class PpooService {

    private val logger = KotlinLogging.logger { }

    fun ping(): Boolean {
        logger.trace { "ppoo service.ping ping" }
        return transaction {
            TransactionManager.current().exec("select 1;") {
                it.next(); it.getString(1)
            }.equals("1")
        }
    }

    fun scatter(pRoomId: String, pUserId: Long, pTotalAmountOfMoney: Long, pTotalNumberOfPeople: Long): String {
        logger.trace { "ppoo service.scatter roomId: $pRoomId, userId: $pUserId, money: $pTotalAmountOfMoney, people: $pTotalNumberOfPeople" }
        var vToken = generateToken()
        transaction {
            val ppooEvents = PpooEventTable.select {
                PpooEventTable.roomId.eq(pRoomId) and PpooEventTable.userId.eq(pUserId)
            }.map { it[PpooEventTable.token] }
            for (i in 0..10) {
                if (!ppooEvents.contains(vToken)) {
                    break
                }
                vToken = generateToken()
            }
            if (ppooEvents.contains(vToken)) {
                throw PpooStatusException(PpooStatusCode.TOKEN_ISSUE_FAIL)
            }

            val scatterId = PpooEventTable.insertAndGetId {
                it[roomId] = pRoomId
                it[userId] = pUserId
                it[token] = vToken
                it[createdAt] = DateTime.now()
                it[totalAmountOfMoney] = pTotalAmountOfMoney
                it[totalNumberOfPeople] = pTotalNumberOfPeople
            }
            // FIXME 분배방식 여러가지??
            // 엔빵으로 나눠주고 마지막에 잔돈 넣어주기
            for (i in 0 until pTotalNumberOfPeople) {
                PpooPrizeTable.insert {
                    it[event] = scatterId.value
                    it[amount] = (pTotalAmountOfMoney / pTotalNumberOfPeople)
                }
            }
            PpooPrizeTable.insert {
                it[event] = scatterId.value
                it[amount] = (pTotalAmountOfMoney / pTotalNumberOfPeople) + (pTotalAmountOfMoney % pTotalNumberOfPeople)
            }
        }
        return vToken
    }


    fun gather(pRoomId: String, pUserId: Long, pToken: String): Long {
        logger.trace { "ppoo service.gather roomId: $pRoomId, userId: $pUserId, token: $pToken" }
        var prizeAmount = 0L
        transaction {
            val event = PpooEventTable.select {
                (PpooEventTable.roomId eq pRoomId) and (PpooEventTable.token eq pToken)
            }.firstOrNull() ?: throw PpooStatusException(PpooStatusCode.NOT_EXISTS)

            if (event[PpooEventTable.userId] == pUserId) {
                throw PpooStatusException(PpooStatusCode.ROLE_FORBIDDEN)
            }
            if (event[PpooEventTable.createdAt] < DateTime.now(DateTimeZone.UTC).minusMinutes(10)) {
                throw PpooStatusException(PpooStatusCode.EXPIRES)
            }

            val prizeDupe = PpooPrizewinnerTable.select {
                PpooPrizewinnerTable.event.eq(event[PpooEventTable.id].value) and
                        (PpooPrizewinnerTable.userId.eq(pUserId))
            }.firstOrNull()
            if (prizeDupe != null) {
                throw PpooStatusException(PpooStatusCode.DUPLICATE)
            }
            val prize = (PpooPrizeTable leftJoin PpooPrizewinnerTable).select {
                (PpooPrizeTable.event eq event[PpooEventTable.id].value) and
                        (PpooPrizewinnerTable.id.isNull())
            }.firstOrNull() ?: throw PpooStatusException(PpooStatusCode.FINISHED)
            PpooPrizewinnerTable.insert {
                it[PpooPrizewinnerTable.event] = event[PpooEventTable.id].value
                it[PpooPrizewinnerTable.prize] = prize[PpooPrizeTable.id].value
                it[PpooPrizewinnerTable.roomId] = pRoomId
                it[PpooPrizewinnerTable.userId] = pUserId
                it[PpooPrizewinnerTable.createdAt] = DateTime.now(DateTimeZone.UTC)
            }
            prizeAmount = prize[PpooPrizeTable.amount]
        }
        return prizeAmount
    }


    fun inspection(pRoomId: String, pUserId: Long, pToken: String) {
        logger.trace { "ppoo service.inspection roomId: $pRoomId, userId: $pUserId, token: $pToken" }
        transaction {
            ((PpooEventTable leftJoin PpooPrizeTable) leftJoin PpooPrizewinnerTable).select {
                PpooEventTable.token.eq(pToken) and PpooEventTable.roomId.eq(pRoomId) and PpooEventTable.userId.eq(
                    pUserId
                )
            }.forEach {
                println(it[PpooEventTable.userId])
            }
        }
    }
}
