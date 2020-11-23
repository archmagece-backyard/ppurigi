import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import mu.KotlinLogging

fun ppooHeader(call: ApplicationCall): Pair<String, Long> {
    val roomId = call.request.header("X-ROOM-ID")?.toString()
        ?: throw IllegalArgumentException("X-ROOM-ID must be provided")
    val userId = call.request.header("X-USER-ID")?.toLong()
        ?: throw IllegalArgumentException("X-USER-ID must be provided")
    return Pair(roomId, userId)
}

fun ppooToken(call: ApplicationCall) =
    call.parameters["token"] ?: throw IllegalArgumentException("Path variable token not found")

fun Route.ppurigi(ppurigiService: PpooService) {
    val logger = KotlinLogging.logger { }

    get(Constants.URI_HEALTH) {
        logger.debug { "API ping" }
        call.respond(hashMapOf("healthy" to ppurigiService.ping()))
    }
    route(Constants.URI_BASE) {
        post("/scatter") {
            val (roomId, userId) = ppooHeader(call)
            val requestDto = call.receive<ScatterRequest>()

            logger.debug { "API scatter - roomId: $roomId, userId: $userId, requestDto : $requestDto" }

            val token =
                ppurigiService.scatter(roomId, userId, requestDto.totalAmountOfMoney, requestDto.totalNumberOfPeople)

            call.respond(
                HttpStatusCode.Created, ResponseWrapper(
                    code = PpooStatusCode.SUCCESS.code,
                    message = PpooStatusCode.SUCCESS.message,
                    data = ScatterResponse(token = token)
                )
            )
        }
        post("/gather/{token}") {
            val (roomId, userId) = ppooHeader(call)
            val token = ppooToken(call)

            logger.debug { "API gather - roomId: $roomId, userId: $userId, token : $token" }

            call.respond(HttpStatusCode.Accepted, ppurigiService.gather(roomId, userId, token))
        }
        get("/inspection/{token}") {
            val (roomId, userId) = ppooHeader(call)
            val token = ppooToken(call)

            logger.debug { "API inspection - roomId: $roomId, userId: $userId, token : $token" }

            call.respond(HttpStatusCode.OK, ppurigiService.inspection(roomId, userId, token))
        }
    }
}