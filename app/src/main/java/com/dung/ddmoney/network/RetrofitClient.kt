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
    @Volatile
    private var unauthorizedHandler: (() -> Unit)? = null
    
    fun init(manager: TokenManager) {
        tokenManager = manager
    }

    fun setUnauthorizedHandler(handler: () -> Unit) {
        unauthorizedHandler = handler
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = okhttp3.Interceptor { chain ->
        val original = chain.request()
        val manager = tokenManager
        val token = manager?.getToken()
        
        // Bỏ qua thêm Header nếu là API Auth
        if (original.url.encodedPath.contains("/api/auth/")) {
            return@Interceptor chain.proceed(original)
        }

        if (token != null && manager.isTokenExpired(token)) {
            manager.clearToken()
            unauthorizedHandler?.invoke()
            return@Interceptor chain.proceed(original)
        }

        val response = if (token != null) {
            val requestBuilder = original.newBuilder()
                .header("Authorization", "Bearer $token")
            val request = requestBuilder.build()
            chain.proceed(request)
        } else {
            chain.proceed(original)
        }

        if (response.code == 401 || response.code == 403) {
            manager?.clearToken()
            unauthorizedHandler?.invoke()
        }

        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val instance: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    val expenseBookService: ExpenseBookApiService by lazy {
        retrofit.create(ExpenseBookApiService::class.java)
    }
}
