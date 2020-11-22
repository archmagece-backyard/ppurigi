import java.sql.Timestamp

data class ResponseWrapper<T>(
    val code: String,
    val message: String,
    val data: T,
)

data class ScatterRequest(
    val totalAmountOfMoney: Long,
    val totalNumberOfPeople: Long,
)

data class ScatterResponse(
    val token: String,
)

data class GatherRequest(
    val token: String,
)

data class GatherResponse(
    val amountReceive: Int,
)

data class InspectionRequest(
    val token: String,
)

data class InspectionResponse(
    val timestamp: Timestamp,
    val amountReceive: Int,
)

