package org.lsm.flower_mailing.data.letter

import com.google.gson.annotations.SerializedName

data class CreateLetterResponse(
    @SerializedName("message")
    val message: String,
    val status: Boolean
)