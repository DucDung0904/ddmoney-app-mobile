package com.dung.ddmoney.network.dto

data class AuthRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class TokenRefreshResponse(
    val accessToken: String,
    val refreshToken: String? = null
)

data class UserResponse(
    val id: Long,
    val fullName: String,
    val email: String,
    val avatarUrl: String?
)

data class AvatarUpdateRequest(
    val avatarUrl: String
)

data class GoogleLoginRequest(
    val idToken: String
)

data class GoogleAuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse
)
