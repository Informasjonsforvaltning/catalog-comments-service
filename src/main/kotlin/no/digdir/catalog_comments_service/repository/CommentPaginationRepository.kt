package no.digdir.catalog_comments_service.repository

import jakarta.persistence.EntityManager
import no.digdir.catalog_comments_service.model.CommentDBO
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

@Repository
class CommentPaginationRepository(private val entityManager: EntityManager) {

    fun findPaginated(
        orgNumber: String,
        skip: Long,
        limit: Int,
        sortField: String,
        sortDirection: Sort.Direction
    ): List<CommentDBO> {
        val direction = if (sortDirection == Sort.Direction.ASC) "ASC" else "DESC"
        val query = entityManager.createQuery(
            "SELECT c FROM CommentDBO c WHERE c.orgNumber = :orgNumber ORDER BY c.$sortField $direction, c.id $direction",
            CommentDBO::class.java
        )
        query.setParameter("orgNumber", orgNumber)
        query.firstResult = skip.toInt()
        query.maxResults = limit
        return query.resultList
    }

    fun countByOrgNumber(orgNumber: String): Long {
        val query = entityManager.createQuery(
            "SELECT COUNT(c) FROM CommentDBO c WHERE c.orgNumber = :orgNumber",
            Long::class.javaObjectType
        )
        query.setParameter("orgNumber", orgNumber)
        return query.singleResult
    }
}
