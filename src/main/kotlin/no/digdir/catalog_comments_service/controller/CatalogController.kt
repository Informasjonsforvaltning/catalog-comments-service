package no.digdir.catalog_comments_service.controller

import no.digdir.catalog_comments_service.model.Comment
import no.digdir.catalog_comments_service.service.CommentService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
@RequestMapping("/{orgNumber}")
class CatalogController(private val commentService: CommentService) {

    @PreAuthorize("@authorizer.hasOrgReadPermission(#jwt, #orgNumber)")
    @GetMapping
    fun getComments(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable orgNumber: String
    ): ResponseEntity<List<Comment>> =
        ResponseEntity(
            commentService.getCommentsByOrgNumber(orgNumber),
            HttpStatus.OK
        )
}
