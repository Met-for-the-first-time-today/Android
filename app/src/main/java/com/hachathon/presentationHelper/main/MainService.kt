package com.hachathon.presentationHelper.main

import com.hachathon.presentationHelper.main.data.MainDataCrateRequest
import com.hachathon.presentationHelper.main.data.MainDataDeleteResponse
import com.hachathon.presentationHelper.main.data.MainDataDetailResponse
import com.hachathon.presentationHelper.main.data.MainDataResponse
import com.hachathon.presentationHelper.main.data.MainDataUpdateRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface MainService {
    @GET("script/search")
    fun getMainList(
        @Query("token") token: String,
    ): Call<MainDataResponse>


    @GET("script/search")
    fun searchData(
        @Query("token") token: String,
        @Query("index") index: Int,
    ): Call<MainDataDetailResponse>

    @POST("script/create")
    fun createData(
        @Body request: MainDataCrateRequest,
    ): Call<MainDataDetailResponse>

    @PUT("script/edit")
    fun editData(
        @Body request: MainDataUpdateRequest,
    ): Call<MainDataDetailResponse>

    @DELETE("script/delete")
    fun deleteData(
        @Query("token") token: String,
        @Query("index") index: Int,
    ): Call<MainDataDeleteResponse>
}