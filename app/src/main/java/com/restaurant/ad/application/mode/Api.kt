package com.restaurant.ad.application.mode

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface Api {

    @POST("file/upload")
    @Multipart
    fun uploadFile(@Part partList: List<MultipartBody.Part>, @Query("type") type: String): Call<BaseEntity<List<String>>>

}