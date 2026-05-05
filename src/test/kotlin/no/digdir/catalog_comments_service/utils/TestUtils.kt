package no.digdir.catalog_comments_service.utils

import no.digdir.catalog_comments_service.utils.ApiTestContext.Companion.postgresContainer
import org.flywaydb.core.Flyway
import org.springframework.http.*
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import java.sql.DriverManager
import java.sql.Timestamp
import java.time.LocalDateTime


fun apiGet(port: Int, endpoint: String, acceptHeader: String?): Map<String, Any> {

    return try {
        val connection = URL("http://localhost:$port$endpoint").openConnection() as HttpURLConnection
        if (acceptHeader != null) connection.setRequestProperty("Accept", acceptHeader)
        connection.connect()

        if (isOK(connection.responseCode)) {
            val responseBody = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            mapOf(
                "body" to responseBody,
                "header" to connection.headerFields,
                "status" to connection.responseCode
            )
        } else {
            mapOf(
                "status" to connection.responseCode,
                "header" to " ",
                "body" to " "
            )
        }
    } catch (e: Exception) {
        mapOf(
            "status" to e.toString(),
            "header" to " ",
            "body" to " "
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
            "status" to e.statusCode.value(),
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
    Flyway.configure()
        .dataSource(postgresContainer.getJdbcUrl(), DB_USER, DB_PASSWORD)
        .cleanDisabled(false)
        .load()
        .also { it.clean(); it.migrate() }

    val conn = DriverManager.getConnection(postgresContainer.getJdbcUrl(), DB_USER, DB_PASSWORD)

    conn.createStatement().execute("DELETE FROM comments")
    conn.createStatement().execute("DELETE FROM users")

    val userStmt = conn.prepareStatement("INSERT INTO users (id, name, email) VALUES (?, ?, ?)")
    for (user in listOf(USER_1, WRONG_USER)) {
        userStmt.setString(1, user.id)
        userStmt.setString(2, user.name)
        userStmt.setString(3, user.email)
        userStmt.addBatch()
    }
    userStmt.executeBatch()
    userStmt.close()

    val commentStmt = conn.prepareStatement(
        "INSERT INTO comments (id, created_date, last_changed_date, topic_id, org_number, user_id, comment) VALUES (?, ?, ?, ?, ?, ?, ?)"
    )

    val normalComments = listOf(COMMENT_0, COMMENT_1, COMMENT_2, COMMENT_WRONG_ORG, COMMENT_TO_BE_DELETED)
    for (c in normalComments) {
        commentStmt.setString(1, c.id)
        commentStmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()))
        commentStmt.setTimestamp(3, null)
        commentStmt.setString(4, TOPIC_ID)
        commentStmt.setString(5, ORG_NUMBER)
        commentStmt.setString(6, "1924782563")
        commentStmt.setString(7, c.comment)
        commentStmt.addBatch()
    }

    val wrongUserComments = listOf(COMMENT_WRONG_USER)
    for (c in wrongUserComments) {
        commentStmt.setString(1, c.id)
        commentStmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()))
        commentStmt.setTimestamp(3, null)
        commentStmt.setString(4, TOPIC_ID)
        commentStmt.setString(5, ORG_NUMBER)
        commentStmt.setString(6, "123")
        commentStmt.setString(7, c.comment)
        commentStmt.addBatch()
    }

    commentStmt.executeBatch()
    commentStmt.close()
    conn.close()
}
