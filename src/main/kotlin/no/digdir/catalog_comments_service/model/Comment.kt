package no.digdir.catalog_comments_service.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.*

@Document(collection = "comments")
data class CommentDBO(
    @Id
    val id:String,
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    val createdDate: LocalDateTime,
    val lastChangedDate: LocalDateTime? = null,
    val topicId:String?,
    val orgNumber:String?,
    var user:String?,
    val comment:String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Comment(
    val id:String? = null,
    val createdDate: LocalDateTime? = null,
    val lastChangedDate: LocalDateTime? = null,
    val topicId:String? = null,
    val orgNumber:String? = null,
    var user:UserDBO? = null,
    val comment:String? = null
)
