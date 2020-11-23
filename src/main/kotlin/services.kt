import mu.KotlinLogging
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

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
        val vToken = generateToken()
        transaction {
            // FIXME check duplicate
            val scatterId = PpooEventTable.insertAndGetId {
                it[roomId] = pRoomId
                it[userId] = pUserId
                it[token] = generateToken()
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


    fun gather(pRoomId: String, pUserId: Long, pToken: String) {
        logger.trace { "ppoo service.gather roomId: $pRoomId, userId: $pUserId, token: $pToken" }
        transaction {
            val scatter = PpooEventTable.select {
                (PpooEventTable.roomId eq pRoomId) and (PpooEventTable.userId eq pUserId) and (PpooEventTable.token eq pToken)
            }.firstOrNull() ?: throw IllegalAccessError("요청한 값이 없습니다")
            val treasureList = (PpooPrizeTable leftJoin PpooPrizewinnerTable).select {
                (PpooPrizeTable.event eq scatter[PpooEventTable.id].value) and
                        (PpooPrizewinnerTable.id.isNull())
            }.toList()
            treasureList.forEach {
                it[PpooPrizeTable.id]
            }
        }
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
