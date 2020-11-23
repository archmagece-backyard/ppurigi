import org.joda.time.DateTime
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
    val amountGathered: Long,
    val userId: Long,
)

//data class InspectionRequest(
//    @field:Size(min = 0, max = 3)
//    @field:NotNull
//    val token: String,
//)


data class InspectionResponse(
    val createdAt: DateTime,
    val totalAmountOfMoney: Long,
    val sumAmountGathered: Long,
    // 받은금액, 받은사용자 아이디
    val gathers: List<GatherResponse>,
)

