package org.lsm.flower_mailing.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * A generic wrapper for all standard API responses.
 *
 * @param T The type of the data field.
 * @property status The status of the request (e.g., "success", "error").
 * @property message A descriptive message from the server.
 * @property data The payload containing the requested resource(s).
 */
data class ApiResponse<T>(
        @SerializedName("status") val status: String,
        @SerializedName("message") val message: String,
        @SerializedName("data") val data: T
)
