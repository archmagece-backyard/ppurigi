import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import mu.KotlinLogging
import java.lang.Exception
import java.lang.IllegalArgumentException
import kotlin.math.log

fun Route.ppurigi(ppurigiService: PpurigiService) {
    val logger = KotlinLogging.logger { }

    route("/ppurigi") {
        get("/health") {
            call.respond(hashMapOf("healthy" to ppurigiService.ping()))
        }
        post("/scatter") {
            val roomId = call.request.header("X-ROOM-ID")?.toLong() ?: throw IllegalArgumentException("X-ROOM-ID must be provided")
            val userId = call.request.header("X-USER-ID")?.toLong() ?: throw IllegalArgumentException("X-ROOM-ID must be provided")
            val requestDto = call.receive<ScatterRequest>()

            val token = ppurigiService.scatter(roomId, userId, requestDto.totalAmountOfMoney, requestDto.totalNumberOfPeople)

            call.respond(
                HttpStatusCode.Created, ResponseWrapper<ScatterResponse>(
                    code = "PASS",
                    message = "success",
                    data = ScatterResponse(token = token)
                )
            )
        }
        post("/gather/{token}") {
            val roomId = call.request.header("X-ROOM-ID")?.toLong() ?: throw IllegalArgumentException("X-ROOM-ID must be provided")
            val userId = call.request.header("X-USER-ID")?.toLong() ?: throw IllegalArgumentException("X-ROOM-ID must be provided")
            val token = call.parameters["token"] ?: throw IllegalArgumentException("Path variable token not found")

            call.respond(HttpStatusCode.Accepted, ppurigiService.gather(roomId, userId, token))
        }
        get("/inspection/{token}") {
            val roomId = call.request.header("X-ROOM-ID")?.toLong() ?: throw IllegalArgumentException("X-ROOM-ID must be provided")
            val userId = call.request.header("X-USER-ID")?.toLong() ?: throw IllegalArgumentException("X-ROOM-ID must be provided")
            val token = call.parameters["token"] ?: throw IllegalArgumentException("Path variable token not found")

            logger.debug { "token : $token" }
            call.respond(HttpStatusCode.OK, ppurigiService.inspect(roomId, userId, token))
        }
    }
}