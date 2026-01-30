package org.lsm.flower_mailing.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.lsm.flower_mailing.data.auth.ForgotPasswordRequest
import org.lsm.flower_mailing.data.auth.ForgotPasswordResponse
import org.lsm.flower_mailing.data.auth.LoginRequest
import org.lsm.flower_mailing.data.auth.LoginResponse
import org.lsm.flower_mailing.data.auth.LogoutRequest
import org.lsm.flower_mailing.data.auth.LogoutResponse
import org.lsm.flower_mailing.data.auth.User
import org.lsm.flower_mailing.data.letter.CreateLetterResponse
import org.lsm.flower_mailing.data.letter.LetterDetailResponse
import org.lsm.flower_mailing.data.letter.LetterListResponse
import org.lsm.flower_mailing.data.letter.UpdateLetterRequest
import org.lsm.flower_mailing.data.model.request.ChangePasswordRequest
import org.lsm.flower_mailing.data.model.request.UpdateProfileRequest
import org.lsm.flower_mailing.data.model.response.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Legacy monolithic API service.
 *
 * @deprecated Use the modular APIs in `org.lsm.flower_mailing.data.source.remote.api` instead.
 * - [org.lsm.flower_mailing.data.source.remote.api.AuthApi]
 * - [org.lsm.flower_mailing.data.source.remote.api.OutgoingLetterApi]
 * - [org.lsm.flower_mailing.data.source.remote.api.IncomingLetterApi]
 */
@Deprecated(
        "This interface is deprecated. Use the specific API interfaces provided by RetrofitClient."
)
interface ApiService {
        @POST("auth/login") suspend fun login(@Body request: LoginRequest): LoginResponse

        @POST("auth/forgot-password")
        suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ForgotPasswordResponse

        @POST("auth/logout") suspend fun logout(@Body request: LogoutRequest): LogoutResponse

        @Multipart
        @POST("letters")
        suspend fun createLetter(
                @Part("data") data: RequestBody,
                @Part file_surat: MultipartBody.Part?
        ): CreateLetterResponse

        @GET("letters")
        suspend fun getLetters(
                @Query("jenis_surat") jenisSurat: String,
                @Query("sifat") status: String
        ): LetterListResponse

        @GET("letters/{id}")
        suspend fun getLetterById(@Path("id") letterId: String): LetterDetailResponse

        @PUT("letters/{id}")
        suspend fun updateLetter(
                @Path("id") letterId: String,
                @Body request: UpdateLetterRequest
        ): LetterListResponse

        @GET("settings/profile") suspend fun getMyProfile(): ApiResponse<User>

        @PUT("settings/profile")
        suspend fun updateMyProfile(@Body body: UpdateProfileRequest): ApiResponse<User>

        @PUT("settings/change-password")
        suspend fun changePassword(@Body body: ChangePasswordRequest): ApiResponse<Any?>
}
