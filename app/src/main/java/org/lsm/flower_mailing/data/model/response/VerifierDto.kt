package org.lsm.flower_mailing.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a Verifier (Manager KPP or similar).
 *
 * @property id The user ID of the verifier.
 * @property name The full name of the verifier.
 * @property role The role of the verifier (e.g., "manajer_kpp").
 */
data class VerifierDto(
        val id: Int,
        @SerializedName("name")
        val name:
                String, // Or "username" depending on API, but Postman usually returns name or user
        // object
        @SerializedName("role") val role: String?
)
