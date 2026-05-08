package no.digdir.catalog_comments_service.repository

import no.digdir.catalog_comments_service.model.CommentDBO
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CommentDAO : JpaRepository<CommentDBO, String> {
    fun findCommentsByOrgNumber(orgNumber: String): List<CommentDBO>
    fun findCommentsByOrgNumberAndTopicId(orgNumber: String, topicId: String): List<CommentDBO>
    fun findByTopicId(topicId: String): List<CommentDBO>
}
