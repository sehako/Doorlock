package com.example.doorlock

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MyApi {

    @Multipart
    @POST("Api.php?apicall=upload")
    fun uploadImage(
        @Part image: MultipartBody.Part,
        @Part("desc") desc: RequestBody
    ): Call<UploadResponse>

    @Multipart
    @DELETE("Api.php?apicall=upload")
    fun deleteImage(
        @Part image: MultipartBody.Part
    ): Call<UploadResponse>

    companion object {
        operator fun invoke(): MyApi {
            return Retrofit.Builder()
                .baseUrl("http://52.79.155.171//")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MyApi::class.java)
        }
    }
}