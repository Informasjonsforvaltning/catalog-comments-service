package no.digdir.catalogcomments.repository

import no.digdir.catalogcomments.model.UserDBO
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserDAO : JpaRepository<UserDBO, String>
