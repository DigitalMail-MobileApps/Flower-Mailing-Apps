package org.lsm.flower_mailing.data.repository

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.lsm.flower_mailing.data.model.request.CreateIncomingLetterRequest
import org.lsm.flower_mailing.data.model.request.UpdateIncomingLetterRequest
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
            val map = mutableMapOf<String, okhttp3.RequestBody>()
            val textType = "text/plain".toMediaTypeOrNull()

            fun createPart(value: String): okhttp3.RequestBody {
                return value.toRequestBody(textType)
            }

            map["nomor_surat"] = createPart(request.nomorSurat)
            map["pengirim"] = createPart(request.pengirim)
            map["judul_surat"] = createPart(request.judulSurat)
            map["tanggal_surat"] = createPart(request.tanggalSurat)
            map["tanggal_masuk"] = createPart(request.tanggalMasuk)
            map["scope"] = createPart(request.scope)
            map["prioritas"] = createPart(request.prioritas)
            map["isi_surat"] = createPart(request.isiSurat)
            // Status for draft vs submission
            request.status?.let { map["status"] = createPart(it) }
            // fileScanPath is handled by backend via filePart

            val response = api.registerLetter(filePart, map)

            if (response.success) {
                Result.success(response.data ?: throw Exception("Register failed: No data"))
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            android.util.Log.e(
                    "IncomingRepo",
                    "EXCEPTION: ${e.javaClass.simpleName}: ${e.message}",
                    e
            )
            Result.failure(e)
        }
    }
    suspend fun disposeLetter(
            id: Int,
            disposition: String,
            tujuan: String,
            needsReply: Boolean = false
    ): Result<IncomingLetterDto> {
        return try {
            val request =
                    org.lsm.flower_mailing.data.model.request.DispositionRequest(
                            disposition,
                            tujuan,
                            needsReply = needsReply
                    )
            val response = api.disposeLetter(id, request)
            if (response.success) {
                Result.success(response.data ?: throw Exception("Disposition failed: No data"))
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyLetters(): Result<List<IncomingLetterDto>> {
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

    suspend fun archiveLetter(id: Int): Result<IncomingLetterDto> {
        return try {
            val response = api.archiveLetter(id)
            if (response.success) {
                Result.success(response.data ?: throw Exception("Archive failed: No data"))
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
            if (response.success) {
                Result.success(response.data ?: emptyList())
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyDispositions(): Result<List<IncomingLetterDto>> {
        return try {
            val response = api.getMyDispositions()
            if (response.success) {
                Result.success(response.data ?: emptyList())
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateLetter(
            id: Int,
            request: UpdateIncomingLetterRequest
    ): Result<IncomingLetterDto> {
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

    suspend fun getLettersNeedingReply(): Result<List<IncomingLetterDto>> {
        return try {
            val response = api.getLettersNeedingReply()
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
