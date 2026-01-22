package org.lsm.flower_mailing.data.source.remote.api

import org.lsm.flower_mailing.data.model.request.UpdateOutgoingLetterRequest
import org.lsm.flower_mailing.data.model.response.ApiResponse
import org.lsm.flower_mailing.data.model.response.OutgoingLetterDto
import org.lsm.flower_mailing.data.model.response.VerifierDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Interface defining endpoints for the "Surat Keluar" (Outgoing Letter) workflow.
 *
 * This workflow involves:
 * 1. Staff creates a DRAFT.
 * 2. Staff requests verification (Status -> PERLU_VERIFIKASI).
 * 3. Manager verifies (Status -> PERLU_PERSETUJUAN).
 * 4. Director approves (Status -> DISETUJUI).
 */
interface OutgoingLetterApi {

        /**
         * Creates a new outgoing letter draft.
         *
         * @param request The letter details including the associated file path.
         * @return The created letter with "draft" status.
         */
        @retrofit2.http.Multipart
        @POST("letters/keluar")
        suspend fun createDraft(
                @retrofit2.http.Part file: okhttp3.MultipartBody.Part,
                @retrofit2.http.PartMap data: Map<String, @JvmSuppressWildcards okhttp3.RequestBody>
        ): ApiResponse<OutgoingLetterDto>

        /**
         * Updates an existing draft or a letter returned for revision.
         *
         * @param id The ID of the letter to update.
         * @param request The updated fields.
         */
        @PUT("letters/keluar/{id}")
        suspend fun updateLetter(
                @Path("id") id: Int,
                @Body request: UpdateOutgoingLetterRequest
        ): ApiResponse<OutgoingLetterDto>

        /**
         * MANAGER: Approves a letter for verification. Only accessible by users with 'manager'
         * role.
         *
         * @param id The ID of the letter to verify.
         */
        /**
         * MANAGER: Approves a letter for verification. Only accessible by users with 'manager'
         * role.
         *
         * @param id The ID of the letter to verify.
         */
        @POST("letters/keluar/{id}/verify/approve")
        suspend fun verifyLetter(@Path("id") id: Int): ApiResponse<Unit>

        /**
         * DIRECTOR: Approves a letter for final issuance. Only accessible by users with 'direktur'
         * role.
         *
         * @param id The ID of the letter to approve.
         */
        @POST("letters/keluar/{id}/approve")
        suspend fun approveLetter(@Path("id") id: Int): ApiResponse<Unit>

        /** MANAGER: Rejects a letter during verification. */
        @POST("letters/keluar/{id}/verify/reject")
        suspend fun verifyLetterReject(@Path("id") id: Int): ApiResponse<Unit>

        /** DIRECTOR: Rejects a letter during approval. */
        @POST("letters/keluar/{id}/reject")
        suspend fun rejectLetter(@Path("id") id: Int): ApiResponse<Unit>

        /** STAFF: Archives an approved outgoing letter. */
        @POST("letters/keluar/{id}/archive")
        suspend fun archiveLetter(@Path("id") id: Int): ApiResponse<Unit>

        /** STAFF: Retrieves the list of letters created by the current user. */
        @GET("letters/keluar/my") suspend fun getMyLetters(): ApiResponse<List<OutgoingLetterDto>>

        /** MANAGER: Retrieves letters pending verification. */
        @GET("letters/keluar/need-verification")
        suspend fun getLettersNeedingVerification(): ApiResponse<List<OutgoingLetterDto>>

        /** DIRECTOR: Retrieves letters pending approval. */
        @GET("letters/keluar/need-approval")
        suspend fun getLettersNeedingApproval(): ApiResponse<List<OutgoingLetterDto>>

        /**
         * Retrieves a list of available verifiers (e.g., Managers).
         *
         * @param scope Optional scope filter (e.g., "internal", "external").
         */
        @GET("letters/verifiers")
        suspend fun getVerifiers(
                @retrofit2.http.Query("scope") scope: String? = null
        ): ApiResponse<List<VerifierDto>>
}
