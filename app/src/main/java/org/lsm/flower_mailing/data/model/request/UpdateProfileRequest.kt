package org.lsm.flower_mailing.data.model.request

import com.google.gson.annotations.SerializedName

/**
 * Request body for updating the user's profile.
 *
 * @property firstName The new first name.
 * @property lastName The new last name.
 */
data class UpdateProfileRequest(
        @SerializedName("first_name") val firstName: String,
        @SerializedName("last_name") val lastName: String
)
