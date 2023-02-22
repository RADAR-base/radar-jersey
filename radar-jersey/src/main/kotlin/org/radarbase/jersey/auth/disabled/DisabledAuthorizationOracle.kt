package org.radarbase.jersey.auth.disabled

import org.radarbase.auth.authorization.*
import org.radarbase.auth.token.RadarToken

class DisabledAuthorizationOracle : AuthorizationOracle {
    override fun hasPermission(
        identity: RadarToken,
        permission: Permission,
        entity: EntityDetails,
        entityScope: Permission.Entity,
    ): Boolean = true

    override fun hasScope(identity: RadarToken, permission: Permission): Boolean = true

    override fun referentsByScope(
        identity: RadarToken,
        permission: Permission,
    ): AuthorityReferenceSet = AuthorityReferenceSet(global = true)

    override fun RoleAuthority.mayBeGranted(permission: Permission): Boolean = true
}
