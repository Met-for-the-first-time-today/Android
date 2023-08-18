package com.hachathon.presentationHelper.main.data

data class MainDataResponse(
    val message: String,
    val status: String,
    val data: List<MainDataResult>,
)

data class MainDataResult(
    val index: Int,
    val id: String,
    val title: String,
    val data: String,
    val speed: Double,
    val createdAt: String,
    val updatedAt: String,
)

data class MainDataDetailResponse(
    val message: String,
    val status: String,
    val data: MainDataResult,
)

data class MainDataCrateRequest(
    val token: String,
    val title: String,
    val data: String,
    val speed: Float,
)

data class MainDataUpdateRequest(
    val token: String,
    val index: Int,
    val title: String,
    val data: String,
    val speed: Float,
)

data class MainDataDeleteResponse(
    val message: String,
    val status: String,
)