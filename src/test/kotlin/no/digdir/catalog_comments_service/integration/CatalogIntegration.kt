package no.digdir.catalog_comments_service.integration

import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.digdir.catalog_comments_service.model.Comment
import no.digdir.catalog_comments_service.utils.ApiTestContext
import no.digdir.catalog_comments_service.utils.ORG_NUMBER
import no.digdir.catalog_comments_service.utils.WRONG_ORG_NUMBER
import no.digdir.catalog_comments_service.utils.authorizedRequest
import no.digdir.catalog_comments_service.utils.jwk.Access
import no.digdir.catalog_comments_service.utils.jwk.JwtToken
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val mapper: ObjectMapper = jacksonObjectMapper()
    .registerModule(
        JavaTimeModule()
            .addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer(DateTimeFormatter.ISO_ZONED_DATE_TIME))
    )
    .registerModule(Jdk8Module())

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    properties = ["spring.profiles.active=integration-test"],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ContextConfiguration(initializers = [ApiTestContext.Initializer::class])
@Tag("integration")
class CatalogIntegration : ApiTestContext() {

    @Test
    fun `Unauthorized when access token is not included`() {
        val rsp = authorizedRequest("/$ORG_NUMBER", port, "", null, HttpMethod.GET)
        assertEquals(HttpStatus.UNAUTHORIZED.value(), rsp["status"])
    }

    @Test
    fun `Forbidden when requesting comments for org without read access`() {
        val rsp = authorizedRequest(
            "/$WRONG_ORG_NUMBER", port, "",
            JwtToken(Access.ORG_READ).toString(), HttpMethod.GET
        )
        assertEquals(HttpStatus.FORBIDDEN.value(), rsp["status"])
    }

    @Test
    fun `Ok - Returns all comments for organization with read access`() {
        val rsp = authorizedRequest(
            "/$ORG_NUMBER", port, "",
            JwtToken(Access.ORG_READ).toString(), HttpMethod.GET
        )
        assertEquals(HttpStatus.OK.value(), rsp["status"])

        val comments: List<Comment> = mapper.readValue(rsp["body"] as String)
        assertTrue(comments.isNotEmpty(), "Should return comments for the organization")
    }

    @Test
    fun `Ok - Returns all comments for organization with write access`() {
        val rsp = authorizedRequest(
            "/$ORG_NUMBER", port, "",
            JwtToken(Access.ORG_WRITE).toString(), HttpMethod.GET
        )
        assertEquals(HttpStatus.OK.value(), rsp["status"])

        val comments: List<Comment> = mapper.readValue(rsp["body"] as String)
        assertTrue(comments.isNotEmpty(), "Should return comments for the organization")
    }

    @Test
    fun `Ok - Returns all comments for organization with root access`() {
        val rsp = authorizedRequest(
            "/$ORG_NUMBER", port, "",
            JwtToken(Access.ROOT).toString(), HttpMethod.GET
        )
        assertEquals(HttpStatus.OK.value(), rsp["status"])

        val comments: List<Comment> = mapper.readValue(rsp["body"] as String)
        assertTrue(comments.isNotEmpty(), "Should return comments for the organization")
    }
}
