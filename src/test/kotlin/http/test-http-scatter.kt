package http

import Constants
import ScatterRequest
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestHttpScatter : TestBase() {

    @Test
    fun `뿌리기 생성 - header no room id`() = ppurigiServer {
//        val roomId = "R_ABC"
        val userId = 1
        handleRequest {
            method = HttpMethod.Post
            uri = Constants.URI_SCATTER
//            addHeader("X-ROOM-ID", roomId)
            addHeader("X-USER-ID", userId.toString())
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertNotNull(response.content)
        }
    }

    @Test
    fun `뿌리기 생성 - header no user id`() = ppurigiServer {
        val roomId = "R_ABC"
//        val userId = 1
        handleRequest {
            method = HttpMethod.Post
            uri = Constants.URI_SCATTER
            addHeader("X-ROOM-ID", roomId)
//            addHeader("X-USER-ID", userId.toString())
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertNotNull(response.content)
        }
    }

    @Test
    fun `뿌리기 생성 - header no header`() = ppurigiServer {
//        val roomId = "R_ABC"
//        val userId = 1
        handleRequest {
            method = HttpMethod.Post
            uri = Constants.URI_SCATTER
//            addHeader("X-ROOM-ID", roomId)
//            addHeader("X-USER-ID", userId.toString())
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertNotNull(response.content)
        }
    }

    @Test
    fun `뿌리기 생성 - header no content type`() = ppurigiServer {
        val roomId = "R_ABC"
        val userId = 1
        handleRequest {
            method = HttpMethod.Post
            uri = Constants.URI_SCATTER
            addHeader("X-ROOM-ID", roomId)
            addHeader("X-USER-ID", userId.toString())
        }.apply {
            assertEquals(HttpStatusCode.UnsupportedMediaType, response.status())
//            assertNotNull(response.content)
        }
    }

    @Test
    fun `뿌리기 생성 - success`() = ppurigiServer {
        val roomId = "R_ABC"
        val userId = 1
        handleRequest {
            method = HttpMethod.Post
            uri = Constants.URI_SCATTER
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
            assertNotNull(response.content)
        }
    }
}