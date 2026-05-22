package com.dung.ddmoney.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

//    private const val BASE_URL = "http://10.0.2.2:8080/"
    private const val BASE_URL = "http://192.168.0.220:8080/"
//    private const val BASE_URL = "https://abc123.ngrok-free.app/:8080/"

    private var tokenManager: TokenManager? = null
    @Volatile
    private var unauthorizedHandler: (() -> Unit)? = null
    private val refreshLock = Any()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    
    fun init(manager: TokenManager) {
        tokenManager = manager
    }

    fun setUnauthorizedHandler(handler: () -> Unit) {
        unauthorizedHandler = handler
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val refreshHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val authInterceptor = okhttp3.Interceptor { chain ->
        val original = chain.request()
        val manager = tokenManager
        
        // Bỏ qua thêm Header nếu là API Auth
        if (original.url.encodedPath.contains("/api/auth/")) {
            return@Interceptor chain.proceed(original)
        }

        val token = manager?.getToken()
        val accessToken =
            when {
                manager == null -> null
                token.isNullOrBlank() -> refreshAccessTokenIfPossible(manager, null)
                manager.isTokenExpired(token) -> refreshAccessTokenIfPossible(manager, token)
                else -> token
            }

        val response = if (accessToken != null) {
            val requestBuilder = original.newBuilder()
                .header("Authorization", "Bearer $accessToken")
            val request = requestBuilder.build()
            chain.proceed(request)
        } else {
            chain.proceed(original)
        }

        if (response.code == 401 || response.code == 403) {
            val refreshedToken = manager?.let { refreshAccessTokenIfPossible(it, accessToken) }
            if (refreshedToken != null) {
                response.close()
                val retryRequest =
                    original.newBuilder()
                        .header("Authorization", "Bearer $refreshedToken")
                        .build()
                val retryResponse = chain.proceed(retryRequest)
                if (retryResponse.code == 401) {
                    manager.clearToken()
                    unauthorizedHandler?.invoke()
                }
                return@Interceptor retryResponse
            }

            manager?.clearToken()
            unauthorizedHandler?.invoke()
        }

        response
    }

    private fun refreshAccessTokenIfPossible(
        manager: TokenManager,
        tokenThatFailed: String?
    ): String? {
        return synchronized(refreshLock) {
            val currentToken = manager.getToken()
            if (
                !currentToken.isNullOrBlank() &&
                    currentToken != tokenThatFailed &&
                    !manager.isTokenExpired(currentToken)
            ) {
                return@synchronized currentToken
            }

            val refreshToken = manager.getRefreshToken()
            if (refreshToken.isNullOrBlank()) return@synchronized null

            val refreshResult = requestTokenRefresh(refreshToken) ?: return@synchronized null
            manager.saveToken(refreshResult.accessToken)
            refreshResult.refreshToken?.takeIf { it.isNotBlank() }?.let(manager::saveRefreshToken)
            refreshResult.accessToken
        }
    }

    private fun requestTokenRefresh(refreshToken: String): TokenRefreshResult? {
        val body =
            JSONObject()
                .put("refreshToken", refreshToken)
                .toString()
                .toRequestBody(jsonMediaType)
        val request =
            Request.Builder()
                .url("${BASE_URL}api/auth/refresh")
                .post(body)
                .build()

        return runCatching {
            refreshHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null

                val json = JSONObject(response.body?.string().orEmpty())
                val accessToken = json.optString("accessToken").ifBlank { json.optString("token") }
                if (accessToken.isBlank()) return@use null

                val newRefreshToken = json.optString("refreshToken").ifBlank { refreshToken }

                TokenRefreshResult(accessToken, newRefreshToken)
            }
        }.getOrNull()
    }

    private data class TokenRefreshResult(
        val accessToken: String,
        val refreshToken: String?
    )

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
