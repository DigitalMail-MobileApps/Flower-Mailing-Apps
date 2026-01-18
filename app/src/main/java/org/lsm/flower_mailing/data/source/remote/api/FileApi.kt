package org.lsm.flower_mailing.data.source.remote.api

import okhttp3.MultipartBody
import org.lsm.flower_mailing.data.model.response.ApiResponse
import org.lsm.flower_mailing.data.model.response.FileUploadResponse
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/** Interface for file management endpoints. */
interface FileApi {

    /**
     * Uploads a file to the server.
     *
     * This endpoint should be called *before* creating a letter. The returned `file_path` is then
     * included in the letter creation request.
     *
     * @param file The file part to upload.
     * @return The path where the file is stored on the server.
     */
    @Multipart
    @POST("upload")
    suspend fun uploadFile(@Part file: MultipartBody.Part): ApiResponse<FileUploadResponse>
}
