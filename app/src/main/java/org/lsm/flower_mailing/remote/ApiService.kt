package org.lsm.flower_mailing.remote

import com.google.android.datatransport.Priority
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.lsm.flower_mailing.data.auth.ForgotPasswordRequest
import org.lsm.flower_mailing.data.auth.ForgotPasswordResponse
import org.lsm.flower_mailing.data.auth.LoginRequest
import org.lsm.flower_mailing.data.auth.LoginResponse
import org.lsm.flower_mailing.data.auth.LogoutRequest
import org.lsm.flower_mailing.data.auth.LogoutResponse
import org.lsm.flower_mailing.data.letter.CreateLetterResponse
import org.lsm.flower_mailing.data.letter.LetterDetailResponse
import org.lsm.flower_mailing.data.letter.LetterListResponse
import org.lsm.flower_mailing.data.letter.UpdateLetterRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ForgotPasswordResponse

    @POST("auth/logout")
    suspend fun logout(@Body request: LogoutRequest): LogoutResponse

    @Multipart
    @POST("letters")
    suspend fun createLetter(@Part("data") data: RequestBody, @Part file_surat: MultipartBody.Part?): CreateLetterResponse

    @GET("letters")
    suspend fun getLetters(@Query("jenis_surat") jenisSurat: String, @Query("sifat") status: String): LetterListResponse

    @GET("letters/{id}")
    suspend fun getLetterById(@Path("id") letterId: String): LetterDetailResponse

    @PUT("letters/{id}")
    suspend fun updateLetter(@Path("id") letterId: String, @Body request: UpdateLetterRequest): LetterListResponse
}