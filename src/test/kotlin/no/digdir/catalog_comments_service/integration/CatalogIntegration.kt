package no.digdir.catalog_comments_service.integration

import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.digdir.catalog_comments_service.model.Comment
import no.digdir.catalog_comments_service.model.PaginatedResponse
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

    private inline fun <reified T> deserializePaginatedResponse(body: String): PaginatedResponse<T> {
        val type = mapper.typeFactory.constructParametricType(PaginatedResponse::class.java, T::class.java)
        return mapper.readValue(body, type)
    }

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
    fun `Ok - Returns paginated comments for organization with read access`() {
        val rsp = authorizedRequest(
            "/$ORG_NUMBER", port, "",
            JwtToken(Access.ORG_READ).toString(), HttpMethod.GET
        )
        assertEquals(HttpStatus.OK.value(), rsp["status"])

        val response = deserializePaginatedResponse<Comment>(rsp["body"] as String)
        assertTrue(response.items.isNotEmpty(), "Should return comments for the organization")
        assertTrue(response.pagination.totalPages > 0)
        assertEquals(0, response.pagination.page)
        assertEquals(10, response.pagination.size)
    }

    @Test
    fun `Ok - Returns paginated comments for organization with write access`() {
        val rsp = authorizedRequest(
            "/$ORG_NUMBER", port, "",
            JwtToken(Access.ORG_WRITE).toString(), HttpMethod.GET
        )
        assertEquals(HttpStatus.OK.value(), rsp["status"])

        val response = deserializePaginatedResponse<Comment>(rsp["body"] as String)
        assertTrue(response.items.isNotEmpty(), "Should return comments for the organization")
    }

    @Test
    fun `Ok - Returns paginated comments for organization with root access`() {
        val rsp = authorizedRequest(
            "/$ORG_NUMBER", port, "",
            JwtToken(Access.ROOT).toString(), HttpMethod.GET
        )
        assertEquals(HttpStatus.OK.value(), rsp["status"])

        val response = deserializePaginatedResponse<Comment>(rsp["body"] as String)
        assertTrue(response.items.isNotEmpty(), "Should return comments for the organization")
    }

    @Test
    fun `Ok - Custom page and size returns correct number of items`() {
        val rsp = authorizedRequest(
            "/$ORG_NUMBER?page=0&size=2", port, "",
            JwtToken(Access.ORG_READ).toString(), HttpMethod.GET
        )
        assertEquals(HttpStatus.OK.value(), rsp["status"])

        val response = deserializePaginatedResponse<Comment>(rsp["body"] as String)
        assertEquals(2, response.items.size)
        assertEquals(0, response.pagination.page)
        assertEquals(2, response.pagination.size)
    }

    @Test
    fun `Ok - Empty items when page exceeds data`() {
        val rsp = authorizedRequest(
            "/$ORG_NUMBER?page=9999", port, "",
            JwtToken(Access.ORG_READ).toString(), HttpMethod.GET
        )
        assertEquals(HttpStatus.OK.value(), rsp["status"])

        val response = deserializePaginatedResponse<Comment>(rsp["body"] as String)
        assertTrue(response.items.isEmpty(), "Should return empty items for page beyond data")
    }

    @Test
    fun `Bad Request when page exceeds maximum`() {
        val rsp = authorizedRequest(
            "/$ORG_NUMBER?page=10001", port, "",
            JwtToken(Access.ORG_READ).toString(), HttpMethod.GET
        )
        assertEquals(HttpStatus.BAD_REQUEST.value(), rsp["status"])
    }

    @Test
    fun `Bad Request when size exceeds maximum`() {
        val rsp = authorizedRequest(
            "/$ORG_NUMBER?size=101", port, "",
            JwtToken(Access.ORG_READ).toString(), HttpMethod.GET
        )
        assertEquals(HttpStatus.BAD_REQUEST.value(), rsp["status"])
    }

    @Test
    fun `Ok - Sort order ascending`() {
        val rsp = authorizedRequest(
            "/$ORG_NUMBER?sort_order=asc&sort_by=comment", port, "",
            JwtToken(Access.ORG_READ).toString(), HttpMethod.GET
        )
        assertEquals(HttpStatus.OK.value(), rsp["status"])

        val response = deserializePaginatedResponse<Comment>(rsp["body"] as String)
        assertTrue(response.items.isNotEmpty())
        val comments = response.items.mapNotNull { it.comment }
        assertEquals(comments.sorted(), comments, "Comments should be in ascending order")
    }

    @Test
    fun `Ok - Defaults applied when no params`() {
        val rsp = authorizedRequest(
            "/$ORG_NUMBER", port, "",
            JwtToken(Access.ORG_READ).toString(), HttpMethod.GET
        )
        assertEquals(HttpStatus.OK.value(), rsp["status"])

        val response = deserializePaginatedResponse<Comment>(rsp["body"] as String)
        assertEquals(0, response.pagination.page)
        assertEquals(10, response.pagination.size)
    }

    @Test
    fun `Ok - Page 1 returns different items than page 0`() {
        val rsp0 = authorizedRequest(
            "/$ORG_NUMBER?page=0&size=2", port, "",
            JwtToken(Access.ORG_READ).toString(), HttpMethod.GET
        )
        val rsp1 = authorizedRequest(
            "/$ORG_NUMBER?page=1&size=2", port, "",
            JwtToken(Access.ORG_READ).toString(), HttpMethod.GET
        )
        assertEquals(HttpStatus.OK.value(), rsp0["status"])
        assertEquals(HttpStatus.OK.value(), rsp1["status"])

        val page0 = deserializePaginatedResponse<Comment>(rsp0["body"] as String)
        val page1 = deserializePaginatedResponse<Comment>(rsp1["body"] as String)

        assertTrue(page0.items.isNotEmpty(), "Page 0 should have items")
        assertTrue(page1.items.isNotEmpty(), "Page 1 should have items")

        val page0Ids = page0.items.map { it.id }.toSet()
        val page1Ids = page1.items.map { it.id }.toSet()
        assertTrue(page0Ids.intersect(page1Ids).isEmpty(), "Page 0 and page 1 should have no overlapping IDs")
    }

    @Test
    fun `Ok - totalPages is correct for seeded data`() {
        val rsp = authorizedRequest(
            "/$ORG_NUMBER?size=2", port, "",
            JwtToken(Access.ORG_READ).toString(), HttpMethod.GET
        )
        assertEquals(HttpStatus.OK.value(), rsp["status"])

        val response = deserializePaginatedResponse<Comment>(rsp["body"] as String)
        assertTrue(response.pagination.totalPages >= 3, "6 seeded comments / size 2 should give at least 3 totalPages")
    }

    @Test
    fun `Ok - Sort by datetime descending returns items in desc order`() {
        val rsp = authorizedRequest(
            "/$ORG_NUMBER?sort_by=datetime&sort_order=desc", port, "",
            JwtToken(Access.ORG_READ).toString(), HttpMethod.GET
        )
        assertEquals(HttpStatus.OK.value(), rsp["status"])

        val response = deserializePaginatedResponse<Comment>(rsp["body"] as String)
        assertTrue(response.items.isNotEmpty())

        val dates = response.items.mapNotNull { it.createdDate }
        for (i in 0 until dates.size - 1) {
            assertTrue(
                !dates[i].isBefore(dates[i + 1]),
                "Items should be in descending date order: ${dates[i]} should not be before ${dates[i + 1]}"
            )
        }
    }

    @Test
    fun `Ok - Negative page is clamped to 0`() {
        val rsp = authorizedRequest(
            "/$ORG_NUMBER?page=-1", port, "",
            JwtToken(Access.ORG_READ).toString(), HttpMethod.GET
        )
        assertEquals(HttpStatus.OK.value(), rsp["status"])

        val response = deserializePaginatedResponse<Comment>(rsp["body"] as String)
        assertEquals(0, response.pagination.page)
    }

    @Test
    fun `Ok - Size 1 returns exactly one item`() {
        val rsp = authorizedRequest(
            "/$ORG_NUMBER?size=1", port, "",
            JwtToken(Access.ORG_READ).toString(), HttpMethod.GET
        )
        assertEquals(HttpStatus.OK.value(), rsp["status"])

        val response = deserializePaginatedResponse<Comment>(rsp["body"] as String)
        assertEquals(1, response.items.size)
        assertTrue(response.pagination.totalPages >= 6, "6 seeded comments / size 1 should give at least 6 totalPages")
    }
}
