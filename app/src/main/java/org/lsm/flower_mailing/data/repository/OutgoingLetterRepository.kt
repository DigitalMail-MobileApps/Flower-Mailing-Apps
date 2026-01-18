package org.lsm.flower_mailing.data.repository

import okhttp3.MultipartBody
import org.lsm.flower_mailing.data.model.request.CreateOutgoingLetterRequest
import org.lsm.flower_mailing.data.model.response.OutgoingLetterDto
import org.lsm.flower_mailing.data.model.response.VerifierDto
import org.lsm.flower_mailing.data.source.remote.api.FileApi
import org.lsm.flower_mailing.data.source.remote.api.OutgoingLetterApi

/**
 * Repository for managing "Surat Keluar" (Outgoing Letters).
 *
 * Handles the coordination of file uploads and letter creation/verification.
 */
class OutgoingLetterRepository(private val api: OutgoingLetterApi, private val fileApi: FileApi) {

    /**
     * Creates a new draft for an outgoing letter.
     *
     * @param request The letter details (without file path).
     * @param file The PDF file to be attached.
     */
    suspend fun createDraft(
            request: CreateOutgoingLetterRequest, // Helper DTO or parameters
            filePart: MultipartBody.Part
    ): Result<OutgoingLetterDto> {
        return try {
            // 1. Upload File first
            val uploadResponse = fileApi.uploadFile(filePart)
            val filePath = uploadResponse.data.filePath

            // 2. Create the Letter using the returned file path
            val finalRequest = request.copy(filePath = filePath)
            val response = api.createDraft(finalRequest)

            if (response.status == "success") {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLetters(): Result<List<OutgoingLetterDto>> {
        return try {
            val response = api.getLetters()
            if (response.status == "success") {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyLetters(): Result<List<OutgoingLetterDto>> {
        return try {
            val response = api.getMyLetters()
            if (response.status == "success") {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVerifiers(scope: String? = null): Result<List<VerifierDto>> {
        return try {
            val response = api.getVerifiers(scope)
            if (response.status == "success" && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyLetter(id: Int): Result<OutgoingLetterDto> {
        return try {
            val response = api.verifyLetter(id)
            if (response.status == "success") {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun approveLetter(id: Int): Result<OutgoingLetterDto> {
        return try {
            val response = api.approveLetter(id)
            if (response.status == "success") {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLettersNeedingVerification(): Result<List<OutgoingLetterDto>> {
        return try {
            val response = api.getLettersNeedingVerification()
            if (response.status == "success") {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLettersNeedingApproval(): Result<List<OutgoingLetterDto>> {
        return try {
            val response = api.getLettersNeedingApproval()
            if (response.status == "success") {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
