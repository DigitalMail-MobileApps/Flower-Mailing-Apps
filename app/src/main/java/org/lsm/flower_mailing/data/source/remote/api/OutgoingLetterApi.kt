package org.lsm.flower_mailing.data.source.remote.api

import org.lsm.flower_mailing.data.model.request.CreateOutgoingLetterRequest
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
        @POST("letters/keluar")
        suspend fun createDraft(
                @Body request: CreateOutgoingLetterRequest
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
        @POST("letters/keluar/{id}/verify/approve")
        suspend fun verifyLetter(@Path("id") id: Int): ApiResponse<OutgoingLetterDto>

        /**
         * DIRECTOR: Approves a letter for final issuance. Only accessible by users with 'direktur'
         * role.
         *
         * @param id The ID of the letter to approve.
         */
        @POST("letters/keluar/{id}/approve")
        suspend fun approveLetter(@Path("id") id: Int): ApiResponse<OutgoingLetterDto>

        /** Retrieves the list of outgoing letters (generic/all). */
        @GET("letters/keluar") suspend fun getLetters(): ApiResponse<List<OutgoingLetterDto>>

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
