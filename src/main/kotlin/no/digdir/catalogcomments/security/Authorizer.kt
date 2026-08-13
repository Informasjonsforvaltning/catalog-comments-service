package no.digdir.catalogcomments.security

import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

private const val ROLE_ROOT_ADMIN = "system:root:admin"

private fun roleOrgAdmin(orgnr: String) = "organization:$orgnr:admin"

private fun roleOrgWrite(orgnr: String) = "organization:$orgnr:write"

private fun roleOrgRead(orgnr: String) = "organization:$orgnr:read"

@Component("authorizer")
class Authorizer {
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

    private fun hasSysAdminPermission(jwt: Jwt): Boolean {
        val authorities: String? = jwt.claims["authorities"] as? String

        return authorities?.contains(ROLE_ROOT_ADMIN) ?: false
    }
}
