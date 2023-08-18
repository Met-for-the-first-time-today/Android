package com.hachathon.presentationHelper.init.data

data class SignUpResponse(
    val status: String,
    val message: String,
)

data class SignUpRequest(
    val id: String,
    val password: String,
    val username: String,
    )
