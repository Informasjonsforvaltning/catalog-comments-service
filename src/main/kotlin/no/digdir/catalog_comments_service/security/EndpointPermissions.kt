package no.digdir.catalog_comments_service.security

import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

private const val ROLE_ROOT_ADMIN = "system:root:admin"
private fun roleOrgAdmin(orgnr: String) = "organization:$orgnr:admin"
private fun roleOrgWrite(orgnr: String) = "organization:$orgnr:write"
private fun roleOrgRead(orgnr: String) = "organization:$orgnr:read"

@Component
class EndpointPermissions {

    fun hasOrgReadPermission(jwt: Jwt, orgnr: String?): Boolean {
        val authorities: String? = jwt.claims["authorities"] as? String
        return when {
            orgnr == null -> false
            authorities == null -> false
            hasSysAdminPermission(jwt) -> true
            authorities.contains(roleOrgAdmin(orgnr)) -> true
            authorities.contains(roleOrgWrite(orgnr)) -> true
            authorities.contains(roleOrgRead(orgnr)) -> true
            else -> false
        }
    }

    fun hasOrgWritePermission(jwt: Jwt, orgnr: String?): Boolean {
        val authorities: String? = jwt.claims["authorities"] as? String
        return when {
            orgnr == null -> false
            authorities == null -> false
            hasSysAdminPermission(jwt) -> true
            authorities.contains(roleOrgAdmin(orgnr)) -> true
            authorities.contains(roleOrgWrite(orgnr)) -> true
            else -> false
        }
    }

    fun hasSysAdminPermission(jwt: Jwt): Boolean {
        val authorities: String? = jwt.claims["authorities"] as? String

        return authorities?.contains(ROLE_ROOT_ADMIN) ?: false
    }

    fun getUserId(jwt: Jwt): String? =
        jwt.claims["user_name"] as? String

    fun getUserName(jwt: Jwt): String? =
        jwt.claims["name"] as? String

    fun getUserEmail(jwt: Jwt): String? =
        jwt.claims["email"] as? String

}
