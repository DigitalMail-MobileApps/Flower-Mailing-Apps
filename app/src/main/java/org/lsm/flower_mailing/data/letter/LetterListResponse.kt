package org.lsm.flower_mailing.data.letter

import com.google.gson.annotations.SerializedName

data class LetterListResponse(
        @SerializedName("success") val success: Boolean,
        @SerializedName("message") val message: String,
        @SerializedName("data") val data: LetterDataPayload
)
