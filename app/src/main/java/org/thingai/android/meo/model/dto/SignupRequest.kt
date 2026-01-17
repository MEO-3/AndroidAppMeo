package org.thingai.android.meo.model.dto

data class SignupRequest(
    val username: String,
    val email: String,
    val phone_number: String,
    val auth_username: String,
    val password: String
)
