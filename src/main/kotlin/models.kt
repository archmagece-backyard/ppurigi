import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.jodatime.datetime


object PpooEventTable : LongIdTable(name = "t_ppoo_event") {
    val roomId = long("room_id")
    val userId = long("user_id")
    val token = varchar("token", 3).uniqueIndex("idx_ppoo_event_token_uniq")

    val createdAt = datetime("created_at")

    val totalAmountOfMoney = long("amount_of_money")
    val totalNumberOfPeople = long("number_of_people")
}

object PpooPrizeTable : LongIdTable(name = "t_ppoo_prize") {
    val event = long("event").references(PpooEventTable.id, onDelete = ReferenceOption.CASCADE)

    val amount = long("amount")
}

object PpooPrizewinnerTable : LongIdTable(name = "t_ppoo_prizewinner") {
    val event = long("event").references(PpooEventTable.id, onDelete = ReferenceOption.CASCADE)
    val prize = long("prize").references(PpooPrizeTable.id, onDelete = ReferenceOption.CASCADE)

    val roomId: Column<Long> = long("room_id")
    val userId: Column<Long> = long("user_id")
    val createdAt = datetime("created_at")
}
