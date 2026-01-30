package org.lsm.flower_mailing.data.repository

import org.lsm.flower_mailing.data.UserPreferencesRepository
import org.lsm.flower_mailing.data.auth.User
import org.lsm.flower_mailing.data.model.request.ChangePasswordRequest
import org.lsm.flower_mailing.data.model.request.UpdateProfileRequest
import org.lsm.flower_mailing.data.source.remote.api.SettingsApi

/** Repository for managing user settings and profile. */
class SettingsRepository(
        private val settingsApi: SettingsApi,
        private val preferences: UserPreferencesRepository
) {

    /** Fetches the latest profile and updates local preferences. */
    suspend fun getProfile(): Result<User> {
        return try {
            val response = settingsApi.getProfile()
            if (response.success && response.data != null) {
                val user = response.data
                // Update local session with potentially new Name/Email
                preferences.updateUserDetails(
                        role = user.role,
                        name = "${user.firstName} ${user.lastName}",
                        email = user.email
                )
                Result.success(user)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(firstName: String, lastName: String): Result<User> {
        return try {
            val request = UpdateProfileRequest(firstName, lastName)
            val response = settingsApi.updateProfile(request)
            if (response.success && response.data != null) {
                val user = response.data
                // Update local session
                preferences.updateUserDetails(
                        role = user.role,
                        name = "${user.firstName} ${user.lastName}",
                        email = user.email
                )
                Result.success(user)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(request: ChangePasswordRequest): Result<String> {
        return try {
            val response = settingsApi.changePassword(request)
            if (response.success) {
                Result.success(response.message)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
