package org.lsm.flower_mailing.data.source.remote.api

import org.lsm.flower_mailing.data.auth.User
import org.lsm.flower_mailing.data.model.request.ChangePasswordRequest
import org.lsm.flower_mailing.data.model.request.UpdateProfileRequest
import org.lsm.flower_mailing.data.model.response.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

/** Interface defining User Settings and Profile API endpoints. */
interface SettingsApi {

    /**
     * Retrieves the current user's profile information.
     *
     * @return [ApiResponse] containing the [User] object.
     */
    @GET("settings/profile") suspend fun getProfile(): ApiResponse<User>

    /**
     * Updates the user's profile information.
     *
     * @param request [UpdateProfileRequest] containing new name details.
     * @return [ApiResponse] containing the updated [User].
     */
    @PUT("settings/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ApiResponse<User>

    /**
     * Changes the user's password.
     *
     * @param request [ChangePasswordRequest] containing old and new passwords.
     * @return [ApiResponse] with a success message (often null data).
     */
    @PUT("settings/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): ApiResponse<Any?>
}
