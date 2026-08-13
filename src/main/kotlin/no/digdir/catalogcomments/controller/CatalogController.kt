package no.digdir.catalogcomments.controller

import no.digdir.catalogcomments.model.Comment
import no.digdir.catalogcomments.model.PaginatedResponse
import no.digdir.catalogcomments.service.CommentService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin
@RequestMapping("/{orgNumber}")
class CatalogController(private val commentService: CommentService) {
    @PreAuthorize("@authorizer.hasOrgReadPermission(#jwt, #orgNumber)")
    @GetMapping
    fun getComments(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable orgNumber: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(name = "sort_by", defaultValue = "datetime") sortBy: String,
        @RequestParam(name = "sort_order", defaultValue = "desc") sortOrder: String,
    ): ResponseEntity<PaginatedResponse<Comment>> = ResponseEntity(
        commentService.getCommentsByOrgNumberPaginated(orgNumber, page, size, sortBy, sortOrder),
        HttpStatus.OK,
    )
}
