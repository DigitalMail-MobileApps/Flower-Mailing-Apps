package org.lsm.flower_mailing.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a Verifier (Manager KPP or similar).
 *
 * Backend returns GORM Model with uppercase ID, but lowercase username/role.
 */
data class VerifierDto(
        @SerializedName("ID") val id: Int = 0, // GORM Model uses uppercase
        @SerializedName("username") val username: String = "", // User model json tag
        @SerializedName("role") val role: String? = null // User model json tag
)
