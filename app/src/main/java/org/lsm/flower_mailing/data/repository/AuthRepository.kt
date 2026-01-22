package org.lsm.flower_mailing.data.repository

import kotlinx.coroutines.flow.first
import org.lsm.flower_mailing.data.UserPreferencesRepository
import org.lsm.flower_mailing.data.auth.ForgotPasswordRequest
import org.lsm.flower_mailing.data.auth.LoginRequest
import org.lsm.flower_mailing.data.auth.LogoutRequest
import org.lsm.flower_mailing.data.source.remote.api.AuthApi
import org.lsm.flower_mailing.notifications.TopicManager

/**
 * Repository responsible for Authentication operations.
 *
 * It coordinates between the remote [AuthApi] and local [UserPreferencesRepository].
 */
class AuthRepository(
        private val authApi: AuthApi,
        private val preferences: UserPreferencesRepository
) {

    /**
     * Logs the user in, saves their session data securely, and subscribes to notification topics.
     */
    suspend fun login(request: LoginRequest): Result<Unit> {
        return try {
            val response = authApi.login(request)

            if (response.success) {
                val data = response.data ?: throw Exception("Login failed: No data received")
                val user = data.user

                // Save session locally
                preferences.saveLoginData(
                        token = data.accessToken,
                        refreshToken = data.refreshToken,
                        role = user.role,
                        name = "${user.firstName} ${user.lastName}",
                        email = user.email
                )

                // Subscribe to FCM topic based on role
                TopicManager.subscribeForRole(user.role)

                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun forgotPassword(email: String): Result<String> {
        return try {
            val response = authApi.forgotPassword(ForgotPasswordRequest(email))
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            val token = preferences.refreshTokenFlow.first()
            if (!token.isNullOrBlank()) {
                authApi.logout(LogoutRequest(token))
            }
            preferences.clearLoginData()
            // Unsubscribe logic could go here if TopicManager supported it
            Result.success(Unit)
        } catch (e: Exception) {
            preferences.clearLoginData() // Force clear on error
            Result.failure(e)
        }
    }

    val isLoggedIn = preferences.accessTokenFlow
}
