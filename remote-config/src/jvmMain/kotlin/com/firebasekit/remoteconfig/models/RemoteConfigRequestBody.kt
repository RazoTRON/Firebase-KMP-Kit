package com.firebasekit.remoteconfig.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteConfigRequestBody(
    @SerialName("app_id")
    val appId: String,
    @SerialName("app_instance_id")
    val appInstanceId: String,
    @SerialName("app_instance_id_token")
    val appInstanceIdToken: String,
    @SerialName("language_code")
    val languageCode: String = "en-US",
    @SerialName("sdk_version")
    val sdkVersion: String = "10.13.2"
)