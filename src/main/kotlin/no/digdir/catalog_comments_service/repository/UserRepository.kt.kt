package no.digdir.catalog_comments_service.repository

import no.digdir.catalog_comments_service.model.UserDBO
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserDAO : JpaRepository<UserDBO, String>
