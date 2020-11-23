package http

import Constants
import GatherRequest
import ResponseWrapper
import ScatterRequest
import ScatterResponse
import com.google.gson.reflect.TypeToken
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestHttpScenario : TestBase() {

    @Test
    fun call_scatter() = ppurigiServer {
        val roomId = "R_ABC"
        val userId = 1
        logger.debug { "Running Ppurigi Http Test" }
        handleRequest(HttpMethod.Post, Constants.URI_SCATTER) {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            addHeader("X-ROOM-ID", roomId)
            addHeader("X-USER-ID", userId.toString())
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
            val turnsType = object : TypeToken<ResponseWrapper<ScatterResponse>>() {}.type
            val scatterResponse = gson.fromJson<ResponseWrapper<ScatterResponse>>(response.content, turnsType)
            val token = scatterResponse.data.token
            handleRequest(HttpMethod.Post, "${Constants.URI_GATHER}/$token") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader("X-ROOM-ID", roomId)
                addHeader("X-USER-ID", userId.toString())
            }.apply {
                assertEquals(HttpStatusCode.Accepted, response.status())
                assertNotNull(response.content)
            }
            handleRequest(HttpMethod.Get, "${Constants.URI_INSPECTION}/$token") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader("X-ROOM-ID", roomId)
                addHeader("X-USER-ID", userId.toString())
                setBody(
                    gson.toJson(
                        GatherRequest(
                            token = token
                        )
                    )
                )
            }.apply {
                println(response.content)
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }
}