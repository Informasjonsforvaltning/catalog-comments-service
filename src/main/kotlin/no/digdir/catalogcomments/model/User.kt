package no.digdir.catalogcomments.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "users")
data class UserDBO(
    @Id
    @Column(name = "id")
    val id: String,
    @Column(name = "name")
    val name: String? = null,
    @Column(name = "email")
    val email: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class User(val id: String? = null, val userName: String? = null, val name: String? = null, val email: String? = null)
