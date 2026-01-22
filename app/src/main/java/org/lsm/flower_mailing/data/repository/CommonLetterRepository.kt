package org.lsm.flower_mailing.data.repository

import org.lsm.flower_mailing.data.model.response.LetterDto
import org.lsm.flower_mailing.data.source.remote.api.CommonLetterApi

class CommonLetterRepository(private val api: CommonLetterApi) {

    suspend fun getLetterById(id: Int): Result<LetterDto> {
        return try {
            val response = api.getLetterById(id)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteLetter(id: Int): Result<Unit> {
        return try {
            val response = api.deleteLetter(id)
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
