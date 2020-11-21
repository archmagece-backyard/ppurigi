package handler

data class GatherRequest(
    val token: String,
)

data class GatherResponse(
    val amountReceive: Int,
)