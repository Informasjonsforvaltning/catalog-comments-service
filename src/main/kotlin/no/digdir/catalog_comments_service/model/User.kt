package no.digdir.catalog_comments_service.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection = "users")
data class UserDBO(
    @Id val id:String,
    val name:String?,
    val email:String?
    )

@JsonIgnoreProperties(ignoreUnknown = true)
data class User (
    val id: String? = null,
    val userName:String? = null,
    val name: String? = null,
    val email: String? = null
    )
