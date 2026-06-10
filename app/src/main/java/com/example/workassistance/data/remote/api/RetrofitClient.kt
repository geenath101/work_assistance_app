package com.example.workassistance.data.remote.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    /**
     * TODO: Replace with your actual backend base URL.
     * Must end with a trailing slash.
     * Example: "https://api.yourportal.com/v1/"
     */
    //private const val BASE_URL = "https://l877q2ymn5.execute-api.ap-southeast-2.amazonaws.com/dev/"
    private const val BASE_URL = " https://6c96-118-149-77-75.ngrok-free.app/"

    private var accessToken: String? = null

    fun setAccessToken(token: String?) {
        accessToken = token
    }

    fun getAccessToken(): String? = accessToken

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Adds Authorization header with Bearer token if available.
     * Automatically called for all requests when a token is set.
     */
    private val authInterceptor = okhttp3.Interceptor { chain ->
        val originalRequest = chain.request()
        
        val requestWithAuth = if (!accessToken.isNullOrBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }
        
        chain.proceed(requestWithAuth)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)  // Add auth interceptor
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Development toggle.
     * true: use in-memory mock backend (no network required)
     * false: use Retrofit + BASE_URL
     */
    private const val USE_MOCK_API = false

    val apiService: ApiService by lazy {
        if (USE_MOCK_API) {
            MockApiService()
        } else {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
