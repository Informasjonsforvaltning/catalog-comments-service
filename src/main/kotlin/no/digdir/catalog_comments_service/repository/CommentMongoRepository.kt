package no.digdir.catalog_comments_service.repository

import no.digdir.catalog_comments_service.model.CommentDBO
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class CommentMongoRepository(private val mongoTemplate: MongoTemplate) {

    fun findPaginated(
        orgNumber: String,
        skip: Long,
        limit: Int,
        sortField: String,
        sortDirection: Sort.Direction
    ): List<CommentDBO> {
        val query = Query(Criteria.where("orgNumber").`is`(orgNumber))
            .with(Sort.by(sortDirection, sortField).and(Sort.by(sortDirection, "_id")))
            .skip(skip)
            .limit(limit)
        return mongoTemplate.find(query, CommentDBO::class.java)
    }

    fun countByOrgNumber(orgNumber: String): Long {
        val query = Query(Criteria.where("orgNumber").`is`(orgNumber))
        return mongoTemplate.count(query, CommentDBO::class.java)
    }
}
