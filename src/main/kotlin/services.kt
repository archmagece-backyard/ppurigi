import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.lang.Exception

class PpurigiService {

    private val klogger = KotlinLogging.logger { }

    fun ping(): Boolean {
        return transaction {
            TransactionManager.current().exec("select 1;") {
                it.next(); it.getString(1)
            }.equals("1")
        }
    }

    fun scatter(pRoomId: Long, pUserId: Long, pTotalAmountOfMoney: Long, pTotalNumberOfPeople: Long): String {
        val vToken = generateToken()
        transaction {
            val scatterId = Scatter.insertAndGetId {
                it[roomId] = pRoomId
                it[userId] = pUserId
                it[token] = vToken
                it[createdAt] = DateTime.now()
                it[totalAmountOfMoney] = pTotalAmountOfMoney
                it[totalNumberOfPeople] = pTotalNumberOfPeople
            }
            // 엔빵으로 나눠주고 마지막에 잔돈 넣어주기
            for (i in 0 until pTotalNumberOfPeople) {
                Treasure.insert {
                    it[scatter] = scatterId.value
                    it[amount] = (pTotalAmountOfMoney / pTotalNumberOfPeople)
                }
            }
            Treasure.insert {
                it[scatter] = scatterId.value
                it[amount] = (pTotalAmountOfMoney / pTotalNumberOfPeople) + (pTotalAmountOfMoney % pTotalNumberOfPeople)
            }
        }
        return vToken
    }


    fun gather(p_roomId: Long, p_userId: Long, p_token: String) {
        transaction {
            val scatter = Scatter.select {
                (Scatter.roomId eq p_roomId) and (Scatter.userId eq p_userId) and (Scatter.token eq p_token)
            }.firstOrNull() ?: throw Exception("요청한 값이 없음")
            val treasureList = (Treasure leftJoin Hunter).select {
                (Treasure.scatter eq scatter[Scatter.id].value) and
                        (Hunter.id.isNull())
            }.toList()
            treasureList.forEach {
                it[Treasure.id]
            }
        }
    }


    fun inspect(pRoomId: Long, pUserId: Long, pToken: String) {
        transaction {
            ((Scatter leftJoin Treasure) leftJoin Hunter).select {
                Scatter.token.eq(pToken) and Scatter.roomId.eq(pRoomId) and Scatter.userId.eq(pUserId)
            }.forEach {
                println(it[Scatter.userId])
            }
        }
    }
}
