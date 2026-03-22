package com.firebasekit.remoteconfig.models

import kotlinx.serialization.Serializable

@Serializable
data class InstallationResponse(
    val fid: String,
    val refreshToken: String,
    val authToken: AuthToken,
) {
    @Serializable
    data class AuthToken(
        val token: String,
        val expiresIn: String,
    )
}
