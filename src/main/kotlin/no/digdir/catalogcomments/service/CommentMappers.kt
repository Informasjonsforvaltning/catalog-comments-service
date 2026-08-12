package no.digdir.catalogcomments.service

import no.digdir.catalogcomments.model.Comment
import no.digdir.catalogcomments.model.CommentDBO
import no.digdir.catalogcomments.model.UserDBO
import java.time.LocalDateTime
import java.util.UUID

fun CommentDBO.toDTO(userDBO: UserDBO?): Comment =
    Comment(
        id = id,
        createdDate = createdDate,
        topicId = topicId,
        orgNumber = orgNumber,
        user = userDBO,
        comment = comment,
    )

fun Comment.toDBO(
    orgNr: String,
    id: String,
    userId: String?,
): CommentDBO {
    val newCreatedDate = LocalDateTime.now()

    return CommentDBO(
        id = id,
        createdDate = newCreatedDate!!,
        topicId = id,
        orgNumber = orgNr,
        user = userId,
        comment = comment,
    )
}

fun Comment.mapForCreation(
    orgNumber: String,
    topicId: String,
    user: String,
): CommentDBO {
    val newId = UUID.randomUUID().toString()
    val newCreatedDate = LocalDateTime.now()

    return CommentDBO(
        id = newId,
        createdDate = newCreatedDate,
        topicId = topicId,
        orgNumber = orgNumber,
        user = user,
        comment = comment,
    )
}

fun CommentDBO.updateLastChanged(): CommentDBO =
    copy(
        lastChangedDate = LocalDateTime.now(),
    )
