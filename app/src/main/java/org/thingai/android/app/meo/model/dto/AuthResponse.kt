package org.thingai.android.app.meo.model.dto

data class AuthResponse(
    val access_token: String,
    val refresh_token: String,
    val token_type: String
)
