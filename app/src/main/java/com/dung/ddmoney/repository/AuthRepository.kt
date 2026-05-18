package com.dung.ddmoney.repository

import com.dung.ddmoney.network.ApiService
import com.dung.ddmoney.network.TokenManager
import com.dung.ddmoney.network.dto.AuthRequest
import com.dung.ddmoney.network.dto.RegisterRequest

class AuthRepository(
    private val api: ApiService,
    private val tokenManager: TokenManager
) {

    suspend fun login(req: AuthRequest): Result<Unit> = safeCall {
        val response = api.login(req)
        // Lưu token và userId sau khi đăng nhập thành công
        tokenManager.saveToken(response.accessToken)
        tokenManager.saveRefreshToken(response.refreshToken)
        tokenManager.saveUserId(response.user.id)
        tokenManager.saveUserInfo(response.user.fullName, response.user.email, response.user.avatarUrl)
        Unit
    }

    suspend fun loginWithGoogle(idToken: String): Result<Unit> = safeCall {
        val req = com.dung.ddmoney.network.dto.GoogleLoginRequest(idToken)
        val response = api.googleLogin(req)
        tokenManager.saveToken(response.accessToken)
        tokenManager.saveRefreshToken(response.refreshToken)
        tokenManager.saveUserId(response.user.id)
        tokenManager.saveUserInfo(response.user.fullName, response.user.email, response.user.avatarUrl)
        Unit
    }

    suspend fun register(req: RegisterRequest): Result<Unit> = safeCall {
        api.register(req)
        Unit
    }

    fun logout() {
        tokenManager.clearToken()
    }

    fun isLoggedIn(): Boolean {
        return tokenManager.hasValidToken()
    }

    suspend fun fetchCurrentUser(): Result<Unit> = safeCall {
        val user = api.getCurrentUser()
        tokenManager.saveUserInfo(user.fullName, user.email, user.avatarUrl)
        Unit
    }

    suspend fun updateAvatar(url: String): Result<Unit> = safeCall {
        val req = com.dung.ddmoney.network.dto.AvatarUpdateRequest(url)
        val user = api.updateAvatar(req)
        tokenManager.saveUserInfo(user.fullName, user.email, user.avatarUrl)
        Unit
    }
}
