package com.dung.ddmoney.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

//    private const val BASE_URL = "http://10.0.2.2:8080/"
    private const val BASE_URL = "http://192.168.0.220:8080/"
    
    private var tokenManager: TokenManager? = null
    
    fun init(manager: TokenManager) {
        tokenManager = manager
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = okhttp3.Interceptor { chain ->
        val original = chain.request()
        val token = tokenManager?.getToken()
        
        // Bỏ qua thêm Header nếu là API Auth
        if (original.url.encodedPath.contains("/api/auth/")) {
            return@Interceptor chain.proceed(original)
        }

        if (token != null) {
            val requestBuilder = original.newBuilder()
                .header("Authorization", "Bearer $token")
            val request = requestBuilder.build()
            chain.proceed(request)
        } else {
            chain.proceed(original)
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
