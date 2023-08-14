package com.example.doorlock

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface RetrofitInterface {
    @Multipart
    @POST("upload.php")
    fun request(@Part file: MultipartBody.Part): Call<String>

    @DELETE("delete.php")
    fun del_request(
        @Query("name") imageName: String
    ): Call<String>
}