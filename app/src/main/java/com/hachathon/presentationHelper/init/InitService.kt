package com.hachathon.presentationHelper.init

import com.hachathon.presentationHelper.init.data.LoginRequest
import com.hachathon.presentationHelper.init.data.LoginResponse
import com.hachathon.presentationHelper.init.data.SignUpRequest
import com.hachathon.presentationHelper.init.data.SignUpResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface InitService {
    @POST("login")
    fun login(
        @Body request: LoginRequest,
    ): Call<LoginResponse>

    @POST("signup")
    fun signup(
        @Body request: SignUpRequest,
    ): Call<SignUpResponse>
}