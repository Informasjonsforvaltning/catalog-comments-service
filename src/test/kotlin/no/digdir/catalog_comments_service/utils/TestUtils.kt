package no.digdir.catalog_comments_service.utils

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import no.digdir.catalog_comments_service.model.Comment
import no.digdir.catalog_comments_service.model.UserDBO
import no.digdir.catalog_comments_service.utils.ApiTestContext.Companion.mongoContainer
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.springframework.http.*
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime


fun apiGet(port: Int, endpoint: String, acceptHeader: String?): Map<String,Any> {

    return try {
        val connection = URL("http://localhost:$port$endpoint").openConnection() as HttpURLConnection
        if(acceptHeader != null) connection.setRequestProperty("Accept", acceptHeader)
        connection.connect()

        if(isOK(connection.responseCode)) {
            val responseBody = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            mapOf(
                "body"   to responseBody,
                "header" to connection.headerFields,
                "status" to connection.responseCode)
        } else {
            mapOf(
                "status" to connection.responseCode,
                "header" to " ",
                "body"   to " "
            )
        }
    } catch (e: Exception) {
        mapOf(
            "status" to e.toString(),
            "header" to " ",
            "body"   to " "
        )
    }
}

private fun isOK(response: Int?): Boolean = HttpStatus.resolve(response ?: 0)?.is2xxSuccessful ?: false

fun authorizedRequest(
    path: String,
    port: Int,
    body: String? = null,
    token: String? = null,
    httpMethod: HttpMethod,
    accept: MediaType = MediaType.APPLICATION_JSON
): Map<String, Any> {
    val request = RestTemplate()
    request.requestFactory = HttpComponentsClientHttpRequestFactory()
    val url = "http://localhost:$port$path"
    val headers = HttpHeaders()
    headers.accept = listOf(accept)
    token?.let { headers.setBearerAuth(it) }
    headers.contentType = MediaType.APPLICATION_JSON
    val entity: HttpEntity<String> = HttpEntity(body, headers)

    return try {
        val response = request.exchange(url, httpMethod, entity, String::class.java)
        mapOf(
            ("body" to response.body) as Pair<String, String>,
            "header" to response.headers.toString(),
            "status" to response.statusCode.value()
        )

    } catch (e: HttpClientErrorException) {
        mapOf(
            "status" to e.rawStatusCode,
            "header" to " ",
            "body" to e.toString()
        )
    } catch (e: Exception) {
        mapOf(
            "status" to e.toString(),
            "header" to " ",
            "body" to " "
        )
    }

}

fun populate() {
    val connectionString = ConnectionString("mongodb://$MONGO_USER:$MONGO_PASSWORD@localhost:${mongoContainer.getMappedPort(
        MONGO_PORT
    )}/$MONGO_DB_NAME?authSource=admin&authMechanism=SCRAM-SHA-1")
    val pojoCodecRegistry = CodecRegistries.fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(
            PojoCodecProvider.builder().automatic(true).build()))

    val client: MongoClient = MongoClients.create(connectionString)
    val mongoDatabase = client.getDatabase(MONGO_DB_NAME).withCodecRegistry(pojoCodecRegistry)

    val commentCollection = mongoDatabase.getCollection("comments")
    commentCollection.deleteMany(org.bson.Document())
    commentCollection.insertMany(commentDbPopulation())
    commentCollection.insertMany(commentWrongUserDbPopulation())

    val userCollection = mongoDatabase.getCollection("users")
    userCollection.deleteMany(org.bson.Document())
    userCollection.insertMany(userDbPopulation())

    client.close()
}

private fun Comment.mapBDO(userId: String): org.bson.Document =
    org.bson.Document()
        .append("_id", id)
        .append("createdDate", LocalDateTime.now())
        .append("topicId", TOPIC_ID)
        .append("orgNumber", ORG_NUMBER)
        .append("user", userId)
        .append("comment", comment)

fun commentDbPopulation() = listOf(COMMENT_0, COMMENT_1, COMMENT_2, COMMENT_WRONG_ORG, COMMENT_TO_BE_DELETED)
    .map { it.mapBDO("1924782563") }

fun commentWrongUserDbPopulation() = listOf(COMMENT_WRONG_USER)
    .map { it.mapBDO("123") }

fun userDbPopulation() = listOf(USER_1, WRONG_USER)
    .map { it.mapBDO() }

private fun UserDBO.mapBDO(): org.bson.Document =
    org.bson.Document()
        .append("_id", id)
        .append("name", name)
        .append("email", email)
