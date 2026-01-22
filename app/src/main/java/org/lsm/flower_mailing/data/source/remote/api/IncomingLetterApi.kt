package org.lsm.flower_mailing.data.source.remote.api

import org.lsm.flower_mailing.data.model.request.DispositionRequest
import org.lsm.flower_mailing.data.model.request.UpdateIncomingLetterRequest
import org.lsm.flower_mailing.data.model.response.ApiResponse
import org.lsm.flower_mailing.data.model.response.IncomingLetterDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Interface defining endpoints for the "Surat Masuk" (Incoming Letter) workflow.
 *
 * This workflow involves:
 * 1. Staff registers an incoming letter (Status -> BELUM_DISPOSISI).
 * 2. Director adds a disposition (Status -> SUDAH_DISPOSISI).
 */
interface IncomingLetterApi {

        /** STAFF: Retrieves the list of incoming letters registered by the current user. */
        @GET("letters/masuk/my") suspend fun getMyLetters(): ApiResponse<List<IncomingLetterDto>>

        /** Update an incoming letter. */
        @retrofit2.http.PUT("letters/masuk/{id}")
        suspend fun updateLetter(
                @Path("id") id: Int,
                @Body request: UpdateIncomingLetterRequest
        ): ApiResponse<IncomingLetterDto>

        /** Archives an incoming letter. */
        @POST("letters/masuk/{id}/archive")
        suspend fun archiveLetter(@Path("id") id: Int): ApiResponse<IncomingLetterDto>

        /**
         * Registers a new incoming letter from an external source.
         *
         * @param request Details of the incoming letter, including scan file path.
         */
        @retrofit2.http.Multipart
        @POST("letters/masuk")
        suspend fun registerLetter(
                @retrofit2.http.Part file: okhttp3.MultipartBody.Part,
                @retrofit2.http.PartMap data: Map<String, @JvmSuppressWildcards okhttp3.RequestBody>
        ): ApiResponse<IncomingLetterDto>

        /**
         * DIRECTOR: Adds a disposition instruction to the letter. This assigns follow-up tasks to
         * specific users.
         *
         * @param id The ID of the incoming letter.
         * @param request The disposition instructions and assigned users.
         */
        @POST("letters/masuk/{id}/dispose")
        suspend fun disposeLetter(
                @Path("id") id: Int,
                @Body request: DispositionRequest
        ): ApiResponse<IncomingLetterDto>

        /** DIRECTOR: Retrieves incoming letters that require a disposition. */
        @GET("letters/masuk/need-disposition")
        suspend fun getLettersNeedingDisposition(): ApiResponse<List<IncomingLetterDto>>

        /** DIRECTOR: Retrieves history of letters dispositioned by this user. */
        @GET("letters/masuk/my-dispositions")
        suspend fun getMyDispositions(): ApiResponse<List<IncomingLetterDto>>
}
