package com.krisnapranata.tte.data

import com.krisnapranata.tte.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    fun create(baseUrl: String, nik: String): TteApi {
        val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("X-API-KEY", BuildConfig.API_KEY)
                    .header("X-USER", nik)
                    .build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(normalized)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TteApi::class.java)
    }
}
