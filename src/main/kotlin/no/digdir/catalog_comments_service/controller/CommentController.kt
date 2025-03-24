package no.digdir.catalog_comments_service.controller

import no.digdir.catalog_comments_service.model.Comment
import no.digdir.catalog_comments_service.service.CommentService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

private val logger = LoggerFactory.getLogger(CommentController::class.java)

@RestController
@CrossOrigin
@RequestMapping("/{orgNumber}/{topicId}/comment")
class CommentController (private val commentService: CommentService) {

    private fun getUserIdFromJWT(jwt: Jwt): String? =
        jwt.claims["user_name"] as? String

    private fun getUserNameFromJWT(jwt: Jwt): String? =
        jwt.claims["name"] as? String

    private fun getUserEmailFromJWT(jwt: Jwt): String? =
        jwt.claims["email"] as? String

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("@authorizer.hasOrgReadPermission(#jwt, #orgNumber)")
    fun createComment(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable orgNumber: String,
        @PathVariable topicId: String,
        @RequestBody comment: Comment
    ): ResponseEntity<Comment> {
        val userId = getUserIdFromJWT(jwt)
        return when {
            userId == null -> ResponseEntity(HttpStatus.UNAUTHORIZED)
            else -> {
                logger.info("creating comment for ${orgNumber}")
                commentService.insert(comment, orgNumber, topicId, userId, getUserNameFromJWT(jwt), getUserEmailFromJWT(jwt))
                    ?.let { ResponseEntity(it, locationHeaderForCreated(it), HttpStatus.CREATED) }
                    ?: ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }

    @PreAuthorize("@authorizer.hasOrgReadPermission(#jwt, #orgNumber)")
    @GetMapping
    fun getComments(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable orgNumber: String,
        @PathVariable topicId: String
    ): ResponseEntity<List<Comment>> =
        ResponseEntity<List<Comment>>(
            commentService.getCommentsByOrgNumberAndTopicId(orgNumber, topicId),
            HttpStatus.OK
        )

    @PreAuthorize("@authorizer.hasOrgReadPermission(#jwt, #orgNumber)")
    @PutMapping(
        value = ["/{commentId}"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun updateComment(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable orgNumber: String,
        @PathVariable("commentId") commentId: String,
        @RequestBody comment: Comment
    ): ResponseEntity<Comment> {
        val commentDBO = commentService.getCommentDBO(commentId)
        val userId = getUserIdFromJWT(jwt)
        return when {
            commentDBO == null -> ResponseEntity(HttpStatus.NOT_FOUND)
            userId == null -> ResponseEntity(HttpStatus.UNAUTHORIZED)
            userId != commentDBO.user -> ResponseEntity(HttpStatus.FORBIDDEN)
            comment.comment == null -> ResponseEntity(HttpStatus.BAD_REQUEST)
            else -> {
                logger.info("updating comment for ${commentId}")
                commentService.updateComment(commentId, comment, userId)
                    ?.let{ ResponseEntity(it, HttpStatus.OK) }
                    ?: ResponseEntity(HttpStatus.NOT_FOUND)
            }
        }
    }

    @PreAuthorize("@authorizer.hasOrgReadPermission(#jwt, #orgNumber)")
    @DeleteMapping(value = ["/{commentId}"])
    fun deleteComment(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable orgNumber: String,
        @PathVariable("commentId") commentId: String,
    ): ResponseEntity<String> {
        val commentDBO = commentService.getCommentDBO(commentId)
        val userId = getUserIdFromJWT(jwt)
        return when {
            commentDBO == null -> ResponseEntity(HttpStatus.NOT_FOUND)
            userId == null -> ResponseEntity(HttpStatus.UNAUTHORIZED)
            userId != commentDBO.user -> ResponseEntity(HttpStatus.FORBIDDEN)
            else -> {
                logger.info("deleing comment ${commentId}")
                commentService.deleteComment(commentDBO)
                ResponseEntity(HttpStatus.OK)
            }
        }
    }
}

private fun locationHeaderForCreated(comment: Comment): HttpHeaders =
    HttpHeaders().apply {
        add(HttpHeaders.LOCATION, "/comment/${comment.id}")
        add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.LOCATION)
    }
