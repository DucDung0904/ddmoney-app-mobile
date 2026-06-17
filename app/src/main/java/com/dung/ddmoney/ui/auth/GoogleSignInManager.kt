package com.dung.ddmoney.ui.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.dung.ddmoney.util.Constants
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CancellationException

class GoogleSignInManager(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)

    suspend fun signIn(): Result<String> {
        return try {
            val googleIdOption = GetSignInWithGoogleOption.Builder(
                Constants.WEB_CLIENT_ID
            )
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                Result.success(idToken)
            } else {
                Result.failure(Exception("Nhận diện tài khoản thất bại."))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Exception("Đăng nhập Google bị hủy."))
        } catch (e: NoCredentialException) {
            Log.e("GoogleSignIn", "No Google credential available", e)
            Result.failure(
                Exception("Không tìm thấy tài khoản Google. Hãy thêm tài khoản Google vào thiết bị.")
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Error: ${e.message}", e)
            Result.failure(Exception("Không thể đăng nhập Google."))
        }
    }
}
