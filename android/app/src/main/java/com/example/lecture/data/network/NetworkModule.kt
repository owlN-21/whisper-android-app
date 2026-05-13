package com.example.lecture.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private const val BASE_URL = "http://10.0.2.2:8080"

    private val loggingInterceptor: HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    private val okHttpClient: OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

    private val retrofit: Retrofit =
        Retrofit.Builder()
            .baseUrl("$BASE_URL/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val apiService: ApiService =
        retrofit.create(ApiService::class.java)
}