package com.firebasekit.remoteconfig.models

import kotlinx.serialization.Serializable

@Serializable
data class RemoteConfigResponse(
    val entries: Map<String, String> = emptyMap(),
    val state: String? = null,
)
