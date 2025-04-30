package no.digdir.catalog_comments_service.utils

import no.digdir.catalog_comments_service.model.Comment
import no.digdir.catalog_comments_service.model.UserDBO
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap

const val MONGO_USER = "testuser"
const val MONGO_PASSWORD = "testpassword"
const val MONGO_PORT = 27017
const val MONGO_DB_NAME = "catalogComments"

val MONGO_ENV_VALUES: Map<String, String> = ImmutableMap.of(
    "MONGO_INITDB_ROOT_USERNAME", MONGO_USER,
    "MONGO_INITDB_ROOT_PASSWORD", MONGO_PASSWORD
)

val ORG_NUMBER = "246813579"

val TOPIC_ID = "topicId0"

val WRONG_ORG_NUMBER = "wrong-org-number"

val COMMENT_0 = Comment(
    id = "id0",
    comment = "Kommentar 1"
)

val COMMENT_1 = Comment(
    id = "id1",
    comment = "Kommentar 2"
)

val COMMENT_2 = Comment(
    id = "id2",
    comment = "Kommentar med annen topicId"
)

val COMMENT_WRONG_USER = Comment(
    id = "comment-wrong-user",
    comment = "Kommentar med feil bruker"
)

val COMMENT_WRONG_ORG = Comment(
    id = "id-wrong-org",
    comment = "Kommentar"
)

val COMMENT_TO_BE_CREATED = Comment(
    id = "comment-to-be-created",
    comment = "Kommentar 2"
)

val COMMENT_TO_BE_CREATED_1 = Comment(
    id = "comment-to-be-created-1",
    comment = "Kommentar"
)

val COMMENT_TO_BE_UPDATED = Comment(
    comment = "Oppdatert kommentar"
)

val COMMENT_TO_BE_DELETED = Comment(
    id = "comment-to-be-deleted",
    comment = "Slett kommentaren 1"
)

val USER_1 = UserDBO(
    id = "1924782563",
    name = "TEST USER",
    email = "test@test.no"
)

val WRONG_USER = UserDBO(
    id = "123",
    name = "WRONG TEST USER",
    email = "wrong@test.no"
)
