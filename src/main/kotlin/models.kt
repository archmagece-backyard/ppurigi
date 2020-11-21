import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.jodatime.datetime
import org.jetbrains.exposed.sql.money.currency
import org.joda.time.DateTime


object Scatter : LongIdTable(name = "t_scatter") {
    val roomId: Column<Long> = long("room_id")
    val userId: Column<Long> = long("user_id")
    val token: Column<String> = varchar("token", 3).uniqueIndex("idx_ppurigi_token_uniq")

    val createdAt = datetime("created_at")

    //    val amount = currency("currency")
    val totalAmountOfMoney = integer("amount_of_money")
    val totalNumberOfPeople = integer("number_of_people")
}

object Treasure : LongIdTable(name = "t_treasure") {
    val scatter = long("scatter").references(Scatter.id, onDelete = ReferenceOption.CASCADE)

    val amount = integer("amount")
}

object Gather : LongIdTable(name = "t_gather") {
    val scatter = long("scatter").references(Scatter.id, onDelete = ReferenceOption.CASCADE)
    val treasure = long("treasure").references(Treasure.id, onDelete = ReferenceOption.CASCADE)

    val roomId: Column<Long> = long("room_id")
    val userId: Column<Long> = long("user_id")
    val createdAt = datetime("created_at")
}
