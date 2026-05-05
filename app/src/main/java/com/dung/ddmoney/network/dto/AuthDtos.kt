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
    val token: String,
    val userId: Long,
    val fullName: String,
    val email: String
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
