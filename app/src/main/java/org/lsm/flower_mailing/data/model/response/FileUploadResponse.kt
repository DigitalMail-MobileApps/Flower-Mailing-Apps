package org.lsm.flower_mailing.data.model.response

import com.google.gson.annotations.SerializedName

/** Response received after successfully uploading a file. */
data class FileUploadResponse(@SerializedName("file_path") val filePath: String)
