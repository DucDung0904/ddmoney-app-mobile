package com.dung.ddmoney.repository

import retrofit2.HttpException

/**
 * Shared safe-call wrapper for all repository operations.
 * Catches HttpException and general exceptions, returning Result.
 */
suspend fun <T> safeCall(block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: HttpException) {
        val errorBody = e.response()?.errorBody()?.string() ?: e.message()
        Result.failure(Exception("HTTP ${e.code()}: $errorBody"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
