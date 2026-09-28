package com.example.proyectofinal.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface BelvoApi {
    @POST("api/belvo/access-token")
    suspend fun getAccessToken(
        @Header("Authorization") bearerToken: String
    ): BelvoTokenResponse

    @POST("api/belvo/accounts")
    suspend fun getAccounts(
        @Header("Authorization") bearerToken: String,
        @Body request: BelvoLinkRequest
    ): List<BelvoAccount>

    @POST("api/belvo/transactions")
    suspend fun getTransactions(
        @Header("Authorization") bearerToken: String,
        @Body request: BelvoLinkRequest
    ): List<BelvoTransaction>
}

object BelvoRetrofitClient {
    private const val BASE_URL = "https://proyectofinalam.onrender.com/"

    val api: BelvoApi by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BelvoApi::class.java)
    }
}
