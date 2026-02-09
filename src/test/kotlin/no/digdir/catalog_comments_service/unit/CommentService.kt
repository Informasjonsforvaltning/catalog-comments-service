package no.digdir.catalog_comments_service.unit

import no.digdir.catalog_comments_service.model.CommentDBO
import no.digdir.catalog_comments_service.repository.CommentDAO
import no.digdir.catalog_comments_service.repository.CommentMongoRepository
import no.digdir.catalog_comments_service.repository.UserDAO
import no.digdir.catalog_comments_service.service.CommentService
import no.digdir.catalog_comments_service.service.toDBO
import no.digdir.catalog_comments_service.utils.ApiTestContext
import no.digdir.catalog_comments_service.utils.COMMENT_0
import no.digdir.catalog_comments_service.utils.COMMENT_1
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Sort
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Tag("unit")
class CommentService: ApiTestContext() {
    private val commentDAO: CommentDAO = mock()
    private val userDAO: UserDAO = mock()
    private val commentMongoRepository: CommentMongoRepository = mock()
    private val commentService = CommentService(commentDAO, userDAO, commentMongoRepository)

    @Test
    fun `Get all comments by topic id` () {
        whenever(commentDAO.findCommentsByOrgNumberAndTopicId("246813579","topicId0"))
            .thenReturn(listOf(COMMENT_0, COMMENT_1).map { it.toDBO("246813579","topicId0","1924782563") })

        val result = commentService.getCommentsByOrgNumberAndTopicId("246813579", "topicId0")

        assertTrue { result.size == 2 }
    }

    @Test
    fun `Get all comments by topic id without user id` () {
        whenever(commentDAO.findCommentsByOrgNumberAndTopicId("246813579","topicId0"))
            .thenReturn(listOf(COMMENT_0, COMMENT_1).map { it.toDBO("246813579","topicId0",null) })

        val result = commentService.getCommentsByOrgNumberAndTopicId("246813579", "topicId0")

        assertTrue { result.size == 2 }
    }

    @Test
    fun `Paginated - default params return paginated response`() {
        val dbos = createTestDBOs(5)
        whenever(commentMongoRepository.findPaginated(eq("246813579"), eq(0L), eq(10), eq("createdDate"), eq(Sort.Direction.DESC)))
            .thenReturn(dbos)
        whenever(commentMongoRepository.countByOrgNumber("246813579"))
            .thenReturn(5L)

        val result = commentService.getCommentsByOrgNumberPaginated("246813579", 0, 10, "datetime", "desc")

        assertEquals(5, result.items.size)
        assertEquals(0, result.pagination.page)
        assertEquals(10, result.pagination.size)
        assertEquals(1, result.pagination.totalPages)
    }

    @Test
    fun `Paginated - page below min is clamped to 0`() {
        whenever(commentMongoRepository.findPaginated(eq("246813579"), eq(0L), eq(10), eq("createdDate"), eq(Sort.Direction.DESC)))
            .thenReturn(emptyList())
        whenever(commentMongoRepository.countByOrgNumber("246813579"))
            .thenReturn(0L)

        val result = commentService.getCommentsByOrgNumberPaginated("246813579", -5, 10, "datetime", "desc")

        assertEquals(0, result.pagination.page)
    }

    @Test
    fun `Paginated - page exceeding max throws Bad Request`() {
        val exception = assertThrows<ResponseStatusException> {
            commentService.getCommentsByOrgNumberPaginated("246813579", 10001, 10, "datetime", "desc")
        }
        assertEquals(400, exception.statusCode.value())
    }

    @Test
    fun `Paginated - size exceeding max throws Bad Request`() {
        val exception = assertThrows<ResponseStatusException> {
            commentService.getCommentsByOrgNumberPaginated("246813579", 1, 101, "datetime", "desc")
        }
        assertEquals(400, exception.statusCode.value())
    }

    @Test
    fun `Paginated - size below min is clamped to 1`() {
        whenever(commentMongoRepository.findPaginated(eq("246813579"), eq(0L), eq(1), eq("createdDate"), eq(Sort.Direction.DESC)))
            .thenReturn(emptyList())
        whenever(commentMongoRepository.countByOrgNumber("246813579"))
            .thenReturn(0L)

        val result = commentService.getCommentsByOrgNumberPaginated("246813579", 0, 0, "datetime", "desc")

        assertEquals(1, result.pagination.size)
    }

    @Test
    fun `Paginated - unknown sort field defaults to createdDate`() {
        whenever(commentMongoRepository.findPaginated(eq("246813579"), eq(0L), eq(10), eq("createdDate"), eq(Sort.Direction.DESC)))
            .thenReturn(emptyList())
        whenever(commentMongoRepository.countByOrgNumber("246813579"))
            .thenReturn(0L)

        val result = commentService.getCommentsByOrgNumberPaginated("246813579", 0, 10, "unknownField", "desc")

        assertEquals(0, result.pagination.totalPages)
    }

    @Test
    fun `Paginated - totalPages computed correctly`() {
        val dbos = createTestDBOs(10)
        whenever(commentMongoRepository.findPaginated(eq("246813579"), eq(0L), eq(10), eq("createdDate"), eq(Sort.Direction.DESC)))
            .thenReturn(dbos)
        whenever(commentMongoRepository.countByOrgNumber("246813579"))
            .thenReturn(25L)

        val result = commentService.getCommentsByOrgNumberPaginated("246813579", 0, 10, "datetime", "desc")

        assertEquals(3, result.pagination.totalPages)
    }

    @Test
    fun `Paginated - empty results return empty list and totalPages 0`() {
        whenever(commentMongoRepository.findPaginated(eq("246813579"), eq(0L), eq(10), eq("createdDate"), eq(Sort.Direction.DESC)))
            .thenReturn(emptyList())
        whenever(commentMongoRepository.countByOrgNumber("246813579"))
            .thenReturn(0L)

        val result = commentService.getCommentsByOrgNumberPaginated("246813579", 0, 10, "datetime", "desc")

        assertTrue(result.items.isEmpty())
        assertEquals(0, result.pagination.totalPages)
    }

    @Test
    fun `Paginated - sort order asc is applied`() {
        whenever(commentMongoRepository.findPaginated(eq("246813579"), eq(0L), eq(10), eq("createdDate"), eq(Sort.Direction.ASC)))
            .thenReturn(emptyList())
        whenever(commentMongoRepository.countByOrgNumber("246813579"))
            .thenReturn(0L)

        val result = commentService.getCommentsByOrgNumberPaginated("246813579", 0, 10, "datetime", "asc")

        assertTrue(result.items.isEmpty())
    }

    @Test
    fun `Paginated - page at max boundary 10000 is accepted`() {
        whenever(commentMongoRepository.findPaginated(eq("246813579"), eq(100000L), eq(10), eq("createdDate"), eq(Sort.Direction.DESC)))
            .thenReturn(emptyList())
        whenever(commentMongoRepository.countByOrgNumber("246813579"))
            .thenReturn(0L)

        val result = commentService.getCommentsByOrgNumberPaginated("246813579", 10000, 10, "datetime", "desc")

        assertEquals(10000, result.pagination.page)
    }

    @Test
    fun `Paginated - size at max boundary 100 is accepted`() {
        whenever(commentMongoRepository.findPaginated(eq("246813579"), eq(0L), eq(100), eq("createdDate"), eq(Sort.Direction.DESC)))
            .thenReturn(emptyList())
        whenever(commentMongoRepository.countByOrgNumber("246813579"))
            .thenReturn(0L)

        val result = commentService.getCommentsByOrgNumberPaginated("246813579", 0, 100, "datetime", "desc")

        assertEquals(100, result.pagination.size)
    }

    @Test
    fun `Paginated - exactly divisible totalPages`() {
        val dbos = createTestDBOs(3)
        whenever(commentMongoRepository.findPaginated(eq("246813579"), eq(0L), eq(3), eq("createdDate"), eq(Sort.Direction.DESC)))
            .thenReturn(dbos)
        whenever(commentMongoRepository.countByOrgNumber("246813579"))
            .thenReturn(6L)

        val result = commentService.getCommentsByOrgNumberPaginated("246813579", 0, 3, "datetime", "desc")

        assertEquals(2, result.pagination.totalPages)
    }

    @Test
    fun `Paginated - known sort field lastChangedDate maps correctly`() {
        whenever(commentMongoRepository.findPaginated(eq("246813579"), eq(0L), eq(10), eq("lastChangedDate"), eq(Sort.Direction.DESC)))
            .thenReturn(emptyList())
        whenever(commentMongoRepository.countByOrgNumber("246813579"))
            .thenReturn(0L)

        val result = commentService.getCommentsByOrgNumberPaginated("246813579", 0, 10, "lastChangedDate", "desc")

        assertEquals(0, result.items.size)
    }

    @Test
    fun `Paginated - known sort field topicId maps correctly`() {
        whenever(commentMongoRepository.findPaginated(eq("246813579"), eq(0L), eq(10), eq("topicId"), eq(Sort.Direction.DESC)))
            .thenReturn(emptyList())
        whenever(commentMongoRepository.countByOrgNumber("246813579"))
            .thenReturn(0L)

        val result = commentService.getCommentsByOrgNumberPaginated("246813579", 0, 10, "topicId", "desc")

        assertEquals(0, result.items.size)
    }

    @Test
    fun `Paginated - sort order ASC case insensitive`() {
        whenever(commentMongoRepository.findPaginated(eq("246813579"), eq(0L), eq(10), eq("createdDate"), eq(Sort.Direction.ASC)))
            .thenReturn(emptyList())
        whenever(commentMongoRepository.countByOrgNumber("246813579"))
            .thenReturn(0L)

        val result = commentService.getCommentsByOrgNumberPaginated("246813579", 0, 10, "datetime", "ASC")

        assertTrue(result.items.isEmpty())
    }

    @Test
    fun `Paginated - page 2 computes correct skip`() {
        val dbos = createTestDBOs(5)
        whenever(commentMongoRepository.findPaginated(eq("246813579"), eq(10L), eq(5), eq("createdDate"), eq(Sort.Direction.DESC)))
            .thenReturn(dbos)
        whenever(commentMongoRepository.countByOrgNumber("246813579"))
            .thenReturn(15L)

        val result = commentService.getCommentsByOrgNumberPaginated("246813579", 2, 5, "datetime", "desc")

        assertEquals(5, result.items.size)
        assertEquals(2, result.pagination.page)
    }

    private fun createTestDBOs(count: Int): List<CommentDBO> =
        (1..count).map { i ->
            CommentDBO(
                id = "id$i",
                createdDate = LocalDateTime.now(),
                topicId = "topicId0",
                orgNumber = "246813579",
                user = null,
                comment = "Comment $i"
            )
        }
}
