package org.lsm.flower_mailing.remote

import org.lsm.flower_mailing.data.auth.ForgotPasswordRequest
import org.lsm.flower_mailing.data.auth.ForgotPasswordResponse
import org.lsm.flower_mailing.data.auth.LoginRequest
import org.lsm.flower_mailing.data.auth.LoginResponse
import org.lsm.flower_mailing.data.auth.LogoutRequest
import org.lsm.flower_mailing.data.letter.CreateLetterRequest
import org.lsm.flower_mailing.data.letter.Letter
import org.lsm.flower_mailing.data.letter.LetterDetailResponse
import org.lsm.flower_mailing.data.letter.LetterListResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ForgotPasswordResponse

    @POST("auth/logout")
    suspend fun logout(@Body request: LogoutRequest): ForgotPasswordResponse

    @POST("letters")
    suspend fun createLetter(@Body request: CreateLetterRequest): ForgotPasswordResponse

    @GET("letters")
    suspend fun getLetters(@Query("jenis_surat") jenisSurat: String): LetterListResponse

    @GET("letters/{id}")
    suspend fun getLetterById(@Path("id") letterId: Int): LetterDetailResponse

    @PUT("letters/{id}")
    suspend fun updateLetter(@Path("id") letterId: Int, @Body request: CreateLetterRequest): LetterDetailResponse
}
