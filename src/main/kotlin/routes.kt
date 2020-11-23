import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import mu.KotlinLogging

fun ppurigiHeader(call: ApplicationCall): Pair<String, Long> {
    val roomId = call.request.header("X-ROOM-ID")?.toString()
        ?: throw IllegalArgumentException("X-ROOM-ID must be provided")
    val userId = call.request.header("X-USER-ID")?.toLong()
        ?: throw IllegalArgumentException("X-USER-ID must be provided")
    return Pair(roomId, userId)
}

fun Route.ppurigi(ppurigiService: PpurigiService) {
    val logger = KotlinLogging.logger { }

    get(Constants.URI_HEALTH) {
        call.respond(hashMapOf("healthy" to ppurigiService.ping()))
    }
    route(Constants.URI_BASE) {
        post("/scatter") {
            val (roomId, userId) = ppurigiHeader(call)
            val requestDto = call.receive<ScatterRequest>()

            val token =
                ppurigiService.scatter(roomId, userId, requestDto.totalAmountOfMoney, requestDto.totalNumberOfPeople)

            call.respond(
                HttpStatusCode.Created, ResponseWrapper(
                    code = ErrorCode.SUCCESS.code,
                    message = ErrorCode.SUCCESS.message,
                    data = ScatterResponse(token = token)
                )
            )
        }
        post("/gather/{token}") {
            val (roomId, userId) = ppurigiHeader(call)
            val token = call.parameters["token"] ?: throw IllegalArgumentException("Path variable token not found")

            call.respond(HttpStatusCode.Accepted, ppurigiService.gather(roomId, userId, token))
        }
        get("/inspection/{token}") {
            val (roomId, userId) = ppurigiHeader(call)
            val token = call.parameters["token"] ?: throw IllegalArgumentException("Path variable token not found")

            logger.debug { "token : $token" }
            call.respond(HttpStatusCode.OK, ppurigiService.inspect(roomId, userId, token))
        }
    }
}