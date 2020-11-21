import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.html.*

fun HTML.index() {
    head {
        title("Hello from Ktor!")
    }
    body {
        div {
            +"Hello from Ktor"
        }
    }
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        routing {
            get("/") {
                call.respondHtml(HttpStatusCode.OK, HTML::index)
            }
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
    }.start(wait = true)
}