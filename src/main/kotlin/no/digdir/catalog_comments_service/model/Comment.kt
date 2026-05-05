package no.digdir.catalog_comments_service.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "comments")
data class CommentDBO(
    @Id
    @Column(name = "id")
    val id: String = "",

    @Column(name = "created_date", nullable = false)
    val createdDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "last_changed_date")
    val lastChangedDate: LocalDateTime? = null,

    @Column(name = "topic_id")
    val topicId: String? = null,

    @Column(name = "org_number")
    val orgNumber: String? = null,

    @Column(name = "user_id")
    var user: String? = null,

    @Column(name = "comment", columnDefinition = "text")
    val comment: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Comment(
    val id: String? = null,
    val createdDate: LocalDateTime? = null,
    val lastChangedDate: LocalDateTime? = null,
    val topicId: String? = null,
    val orgNumber: String? = null,
    var user: UserDBO? = null,
    val comment: String? = null,
)
