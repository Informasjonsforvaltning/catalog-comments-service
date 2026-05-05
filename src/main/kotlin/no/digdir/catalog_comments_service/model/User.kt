package no.digdir.catalog_comments_service.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.*

@Entity
@Table(name = "users")
data class UserDBO(
    @Id
    @Column(name = "id")
    val id: String = "",

    @Column(name = "name")
    val name: String? = null,

    @Column(name = "email")
    val email: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class User(
    val id: String? = null,
    val userName: String? = null,
    val name: String? = null,
    val email: String? = null,
)
