package org.lsm.flower_mailing.data.repository

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.lsm.flower_mailing.data.model.request.CreateOutgoingLetterRequest
import org.lsm.flower_mailing.data.model.request.UpdateOutgoingLetterRequest
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
            val map = mutableMapOf<String, okhttp3.RequestBody>()
            val textType = "text/plain".toMediaTypeOrNull()
            fun createPart(value: String): okhttp3.RequestBody {
                return value.toRequestBody(textType)
            }

            map["nomor_surat"] = createPart(request.nomorSurat)
            map["pengirim"] = createPart(request.pengirim)
            map["judul_surat"] = createPart(request.judulSurat)
            map["bidang_tujuan"] = createPart(request.tujuan) // Renamed field in simple DTO
            map["isi_surat"] = createPart(request.isiSurat)
            map["scope"] = createPart(request.scope)
            map["jenis_surat"] = createPart(request.jenisSurat)
            map["assigned_verifier_id"] = createPart(request.assignedVerifierId.toString())
            map["status"] = createPart(request.status)
            map["nomor_agenda"] = createPart(request.nomorAgenda)
            map["tanggal_surat"] = createPart(request.tanggalSurat)
            map["tanggal_masuk"] = createPart(request.tanggalMasuk)
            map["kesimpulan"] = createPart(request.kesimpulan)
            // filePath is handled by backend via filePart, ignore request.filePath

            val response = api.createDraft(filePart, map)

            if (response.success) {
                Result.success(response.data ?: throw Exception("Create draft failed: No data"))
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyLetterReject(id: Int): Result<Unit> {
        return try {
            val response = api.verifyLetterReject(id)
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectLetter(id: Int): Result<Unit> {
        return try {
            val response = api.rejectLetter(id)
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun archiveLetter(id: Int): Result<Unit> {
        return try {
            val response = api.archiveLetter(id)
            if (response.success) {
                Result.success(Unit)
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
            if (response.success) {
                Result.success(response.data ?: emptyList())
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
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyLetter(id: Int): Result<Unit> {
        return try {
            val response = api.verifyLetter(id)
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun approveLetter(id: Int): Result<Unit> {
        return try {
            val response = api.approveLetter(id)
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateLetter(
            id: Int,
            request: UpdateOutgoingLetterRequest
    ): Result<OutgoingLetterDto> {
        return try {
            val response = api.updateLetter(id, request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLettersNeedingVerification(): Result<List<OutgoingLetterDto>> {
        return try {
            val response = api.getLettersNeedingVerification()
            if (response.success) {
                Result.success(response.data ?: emptyList())
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
            if (response.success) {
                Result.success(response.data ?: emptyList())
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
