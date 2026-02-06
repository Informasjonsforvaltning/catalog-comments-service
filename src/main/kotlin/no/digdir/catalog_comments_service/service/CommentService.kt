package no.digdir.catalog_comments_service.service

import no.digdir.catalog_comments_service.model.Comment
import no.digdir.catalog_comments_service.model.CommentDBO
import no.digdir.catalog_comments_service.model.PaginatedResponse
import no.digdir.catalog_comments_service.model.Pagination
import no.digdir.catalog_comments_service.model.UserDBO
import no.digdir.catalog_comments_service.repository.CommentDAO
import no.digdir.catalog_comments_service.repository.CommentMongoRepository
import no.digdir.catalog_comments_service.repository.UserDAO
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import kotlin.math.ceil

private val logger = LoggerFactory.getLogger(CommentService::class.java)

@Service
class CommentService(
    private val commentDAO: CommentDAO,
    private val userDAO: UserDAO,
    private val commentMongoRepository: CommentMongoRepository
) {

    companion object {
        const val MIN_PAGE = 1
        const val MAX_PAGE = 10000
        const val MIN_SIZE = 1
        const val MAX_SIZE = 100

        val SORT_FIELD_WHITELIST = mapOf(
            "datetime" to "createdDate",
            "createdDate" to "createdDate",
            "lastChangedDate" to "lastChangedDate",
            "topicId" to "topicId",
            "comment" to "comment"
        )
    }

    private fun createUserIfNotExists(userId: String, name: String? = null, email: String? = null) {
        try {
            if (!userDAO.existsById(userId)) {
                val userDocument = UserDBO(id = userId, name = name, email = email)
                userDAO.insert(userDocument)
            }
        } catch (ex: Exception) {
            logger.error("insert user failed", ex)
        }
    }

    fun insert(comment: Comment, orgNumber: String, topicId: String, userId: String, name: String? = null, email: String? = null): Comment? {

        createUserIfNotExists(userId, name, email)

        if (!userDAO.existsById(userId)) {
            throw object : Exception("User not found") {}
        }
        val newComment: CommentDBO = comment.mapForCreation(orgNumber, topicId, userId)

        return commentDAO
            .insert(newComment ).toDTO(userDAO.findByIdOrNull(userId))
    }

    fun getCommentsByOrgNumber(orgNumber: String): List<Comment> = commentDAO.findCommentsByOrgNumber(orgNumber)
        .map { it.toDTO(it.user?.let { userId -> userDAO.findByIdOrNull(userId) }) }

    fun getCommentsByOrgNumberPaginated(
        orgNumber: String,
        page: Int,
        size: Int,
        sortBy: String,
        sortOrder: String
    ): PaginatedResponse<Comment> {
        val validatedPage = when {
            page > MAX_PAGE -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must not exceed $MAX_PAGE")
            page < MIN_PAGE -> MIN_PAGE
            else -> page
        }

        val validatedSize = when {
            size > MAX_SIZE -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Size must not exceed $MAX_SIZE")
            size < MIN_SIZE -> MIN_SIZE
            else -> size
        }

        val sortField = SORT_FIELD_WHITELIST[sortBy] ?: "createdDate"
        val sortDirection = if (sortOrder.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC

        val skip = (validatedPage - 1).toLong() * validatedSize
        val items = commentMongoRepository.findPaginated(orgNumber, skip, validatedSize, sortField, sortDirection)
        val totalCount = commentMongoRepository.countByOrgNumber(orgNumber)

        val dtos = items.map { it.toDTO(it.user?.let { userId -> userDAO.findByIdOrNull(userId) }) }

        val totalPages = if (totalCount == 0L) 0 else ceil(totalCount.toDouble() / validatedSize).toInt()

        return PaginatedResponse(
            items = dtos,
            pagination = Pagination(totalPages = totalPages, page = validatedPage, size = validatedSize)
        )
    }

    fun getCommentsByOrgNumberAndTopicId(orgNumber: String, topicId: String): List<Comment> =
        commentDAO.findCommentsByOrgNumberAndTopicId(orgNumber, topicId)
            .map { it.toDTO(it.user?.let { userId -> userDAO.findByIdOrNull(userId) }) }

    fun getCommentDBO(id: String): CommentDBO? =
        commentDAO.findByIdOrNull(id)

    fun updateComment(commentId: String, obj: Comment, userId: String): Comment? =
        commentDAO.findByIdOrNull(commentId)
                ?.copy(comment = obj.comment ?: "")
                ?.updateLastChanged()
                ?.let { commentDAO.save(it) }?.toDTO(userDAO.findByIdOrNull(userId))

    fun deleteComment(comment: CommentDBO) =
        commentDAO.delete(comment)
}
