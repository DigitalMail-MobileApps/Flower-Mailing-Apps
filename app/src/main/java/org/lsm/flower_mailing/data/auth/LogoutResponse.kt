package org.lsm.flower_mailing.data.auth

import com.google.gson.annotations.SerializedName

data class LogoutResponseResponse(
    @SerializedName("message")
    val message: String,
    val status: Boolean
)
