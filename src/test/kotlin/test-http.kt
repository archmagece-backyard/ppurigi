import com.google.gson.Gson
import handler.GatherRequest
import handler.ScatterRequest
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import mu.KotlinLogging
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestHttp {

    private val logger = KotlinLogging.logger { }
    private val gson = Gson()

    @Test
    fun call_scatter() = ppurigiServer {
        logger.debug { "Running Ppurigi Test" }
        handleRequest(HttpMethod.Post, "/ppurigi/scatter") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            addHeader("X-ROOM-ID", 1.toString())
            addHeader("X-USER-ID", 1.toString())
            setBody(
                gson.toJson(
                    ScatterRequest(
                        totalAmountOfMoney = 1000_0,
                        totalNumberOfPeople = 10,
                    )
                )
            )
        }.apply {
            assertEquals(HttpStatusCode.Created, response.status())
            handleRequest(HttpMethod.Post, "/ppurigi/gather") {
                addHeader("X-ROOM-ID", 1.toString())
                addHeader("X-USER-ID", 2.toString())
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.Accepted, response.status())
                assertNotNull(response.content)
            }
            val token = "AAA"
            handleRequest(HttpMethod.Get, "/ppurigi/inspection/$token") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    gson.toJson(
                        GatherRequest(
                            token = token
                        )
                    )
                )
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    private fun ppurigiServer(callback: TestApplicationEngine.() -> Unit) {
        withTestApplication(Application::module) { callback() }
    }
}