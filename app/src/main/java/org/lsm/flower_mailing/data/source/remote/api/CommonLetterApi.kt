package org.lsm.flower_mailing.data.source.remote.api

import org.lsm.flower_mailing.data.model.response.ApiResponse
import org.lsm.flower_mailing.data.model.response.LetterDto
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface CommonLetterApi {

    /**
     * Retrieves the details of a specific letter (Internal/External/Incoming).
     *
     * @param id The ID of the letter.
     */
    @GET("letters/{id}") suspend fun getLetterById(@Path("id") id: Int): ApiResponse<LetterDto>

    /**
     * Deletes (soft delete) a letter. Usually allowed only for drafts or by Admin.
     *
     * @param id The ID of the letter to delete.
     */
    @DELETE("letters/{id}") suspend fun deleteLetter(@Path("id") id: Int): ApiResponse<Unit>
}
