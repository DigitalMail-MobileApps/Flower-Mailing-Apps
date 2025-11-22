package org.lsm.flower_mailing.data.settings

import com.google.gson.annotations.SerializedName

data class UserProfile(
    val id: Long,
    val username: String,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?,
    val email: String,
    val role: String,
    val jabatan: String?,
    val atribut: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)