package com.example.doorlock

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitTestClient {
    private const val BASE_URL = "http://52.79.155.171//"
    private var retrofit: Retrofit? = null

    private val gson: Gson = GsonBuilder().setLenient().create()

    val client: Retrofit?
        get() {
            if(retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson)) // json response를 파싱하기 위해 Gson을 설정
                    .build()
            }
            return retrofit
        }
}