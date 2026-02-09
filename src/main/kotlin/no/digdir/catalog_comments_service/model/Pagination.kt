package no.digdir.catalog_comments_service.model

data class Pagination(
    val totalPages: Int,
    val page: Int,
    val size: Int
)

data class PaginatedResponse<T>(
    val items: List<T>,
    val pagination: Pagination
)
