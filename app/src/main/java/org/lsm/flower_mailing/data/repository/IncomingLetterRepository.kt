package org.lsm.flower_mailing.data.repository

import okhttp3.MultipartBody
import org.lsm.flower_mailing.data.model.request.CreateIncomingLetterRequest
import org.lsm.flower_mailing.data.model.response.IncomingLetterDto
import org.lsm.flower_mailing.data.source.remote.api.FileApi
import org.lsm.flower_mailing.data.source.remote.api.IncomingLetterApi

/** Repository for managing "Surat Masuk" (Incoming Letters). */
class IncomingLetterRepository(private val api: IncomingLetterApi, private val fileApi: FileApi) {

    /**
     * Registers a new incoming letter.
     *
     * @param request The letter details.
     * @param filePart The scanned file of the letter.
     */
    suspend fun registerLetter(
            request: CreateIncomingLetterRequest,
            filePart: MultipartBody.Part
    ): Result<IncomingLetterDto> {
        return try {
            // 1. Upload File
            val uploadResponse = fileApi.uploadFile(filePart)
            val filePath = uploadResponse.data.filePath

            // 2. Register Letter
            val finalRequest = request.copy(fileScanPath = filePath)
            val response = api.registerLetter(finalRequest)

            if (response.status == "success") {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun disposeLetter(
            id: Int,
            disposition: String,
            targetUserIds: List<Int>
    ): Result<IncomingLetterDto> {
        return try {
            val request =
                    org.lsm.flower_mailing.data.model.request.DispositionRequest(
                            disposition,
                            targetUserIds
                    )
            val response = api.disposeLetter(id, request)
            if (response.status == "success") {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLetters(): Result<List<IncomingLetterDto>> {
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

    suspend fun getLettersNeedingDisposition(): Result<List<IncomingLetterDto>> {
        return try {
            val response = api.getLettersNeedingDisposition()
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
