package org.lsm.flower_mailing.data.settings

import com.google.gson.annotations.SerializedName

data class SettingsResponse<T>(
    val status: String,
    val message: String,
    val data: T?,
    val errors: Any?
)