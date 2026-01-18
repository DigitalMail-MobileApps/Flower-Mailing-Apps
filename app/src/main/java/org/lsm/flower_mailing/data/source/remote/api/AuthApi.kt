package org.lsm.flower_mailing.data.source.remote.api

import org.lsm.flower_mailing.data.auth.ForgotPasswordRequest
import org.lsm.flower_mailing.data.auth.ForgotPasswordResponse
import org.lsm.flower_mailing.data.auth.LoginRequest
import org.lsm.flower_mailing.data.auth.LoginResponse
import org.lsm.flower_mailing.data.auth.LogoutRequest
import org.lsm.flower_mailing.data.auth.LogoutResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Interface defining Authentication-related API endpoints.
 *
 * This includes managing user sessions (login/logout) and credential recovery.
 */
interface AuthApi {

    /**
     * Authenticates a user with their credentials.
     *
     * @param request Contains email and password.
     * @return [LoginResponse] containing the access token and user profile.
     */
    @POST("auth/login") suspend fun login(@Body request: LoginRequest): LoginResponse

    /**
     * Initiates the password recovery process by sending a reset link/code to the user's email.
     *
     * @param request Contains the user's email.
     * @return [ForgotPasswordResponse] status of the request.
     */
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ForgotPasswordResponse

    /**
     * Terminates the user's session on the server side.
     *
     * @param request Contains specific parameters if needed (e.g., token).
     * @return [LogoutResponse] confirming the session handling.
     */
    @POST("auth/logout") suspend fun logout(@Body request: LogoutRequest): LogoutResponse
}
