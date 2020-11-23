import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.jodatime.datetime


object PpooEventTable : LongIdTable(name = "t_ppoo_event") {
    val roomId = varchar("room_id", 30)
    val userId = long("user_id")
    val token = varchar("token", 3) //.uniqueIndex("idx_ppoo_event_token_uniq")

    val createdAt = datetime("created_at")

    val totalAmountOfMoney = long("amount_of_money")
    val totalNumberOfPeople = long("number_of_people")

    val uniq = uniqueIndex("idx_ppoo_event_uniq", roomId, token)
}

object PpooPrizeTable : LongIdTable(name = "t_ppoo_prize") {
    val event = long("event").references(PpooEventTable.id, onDelete = ReferenceOption.CASCADE)

    val amount = long("amount")
}

object PpooPrizewinnerTable : LongIdTable(name = "t_ppoo_prizewinner") {
    val event = long("event").references(PpooEventTable.id, onDelete = ReferenceOption.CASCADE)
    val prize = long("prize").references(PpooPrizeTable.id, onDelete = ReferenceOption.CASCADE)

    // event.roomId 와 같은 값이지만... user_id와 pair라서 함께 저장
    val roomId = varchar("room_id", 30)
    val userId: Column<Long> = long("user_id")
    val createdAt = datetime("created_at")

    val uniq = uniqueIndex("idx_ppoo_pricewinner_uinq", event, prize)
}
