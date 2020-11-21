package handler


data class ScatterRequest(
    val totalAmountOfMoney: Int,
    val totalNumberOfPeople: Int,
)

data class ScatterResponse(
    val token: String,
)