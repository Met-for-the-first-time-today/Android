package com.hachathon.presentationHelper.init.data

data class LoginResponse(
    val status: String,
    val message: String,
    val data: String,
)

data class LoginRequest(
    val id: String,
    val password: String,
)
