import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import mu.KotlinLogging
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.*

class TestIntegration {
    companion object {
        const val IMAGE_NAME = "mariadb"
        const val TAG_NAME = "10"
        val databaseServer: MariadbContainerAdapter = MariadbContainerAdapter("$IMAGE_NAME:$TAG_NAME")
            .withDatabaseName("test-db")
            .withUsername("user01")
            .withPassword("pass01")
    }

    private val logger = KotlinLogging.logger { }
    private val gson = Gson()

    @BeforeTest
    fun before() {
        databaseServer.start()
        System.setProperty("testing", "true")
        System.setProperty("db_type", "maria")
        System.setProperty("maria.username", "user01")
        System.setProperty("maria.password", "pass01")
        System.setProperty("maria.jdbcUrl", databaseServer.jdbcUrl)
    }

    @AfterTest
    fun after() {
        databaseServer.close()
    }

    @Test
    fun call_scatter() = ppurigiServer {
        transaction {
            SchemaUtils.create(Scatter, Treasure, Hunter)
        }
        val roomId = 1
        val userId = 1
        logger.debug { "Running Ppurigi Http Test" }
        handleRequest(HttpMethod.Post, "/ppurigi/scatter") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            addHeader("X-ROOM-ID", roomId.toString())
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
            handleRequest(HttpMethod.Post, "/ppurigi/gather/$token") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader("X-ROOM-ID", roomId.toString())
                addHeader("X-USER-ID", userId.toString())
            }.apply {
                assertEquals(HttpStatusCode.Accepted, response.status())
                assertNotNull(response.content)
            }
            handleRequest(HttpMethod.Get, "/ppurigi/inspection/$token") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader("X-ROOM-ID", roomId.toString())
                addHeader("X-USER-ID", userId.toString())
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
        // TODO test resources
        // TODO inject config
        withTestApplication(Application::module)  { callback() }
    }
}