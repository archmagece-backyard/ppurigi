package handler

import java.sql.Timestamp

data class InspectionRequest(
    val token: String,
)

data class InspectionResponse(
    val timestamp: Timestamp,
    val amountReceive: Int,
)