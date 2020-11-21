import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import mu.KotlinLogging

fun Route.ppurigi() {
    val logger = KotlinLogging.logger { }

    route("/ppurigi") {
        post("/scatter") {
            call.respond(HttpStatusCode.Created, "scatter")
        }
        post("/gather") {
            call.respond(HttpStatusCode.Accepted, "gather")
        }
        get("/inspection/{token}") {
            call.respond(HttpStatusCode.OK, "inspection")
        }
    }
}