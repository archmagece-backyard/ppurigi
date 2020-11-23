import java.sql.Timestamp
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class ResponseWrapper<T>(
    val code: String,
    val message: String,
    val data: T,
)

data class ScatterRequest(
    @field:Min(0)
    @field:NotNull
    val totalAmountOfMoney: Long,
    @field:Min(0)
    @field:NotNull
    val totalNumberOfPeople: Long,
)


data class ScatterResponse(
    val token: String,
)

data class GatherRequest(
    @field:Size(min = 0, max = 3)
    @field:NotNull
    val token: String,
)

data class GatherResponse(
    val amountReceive: Int,
)

data class InspectionRequest(
    @field:Size(min = 0, max = 3)
    @field:NotNull
    val token: String,
)

data class InspectionResponse(
    val timestamp: Timestamp,
    val amountReceive: Int,
)

