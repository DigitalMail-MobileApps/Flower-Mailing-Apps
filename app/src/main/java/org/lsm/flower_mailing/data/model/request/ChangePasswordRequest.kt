package org.lsm.flower_mailing.data.model.request

import com.google.gson.annotations.SerializedName

/**
 * Request body for changing the user's password.
 *
 * @property oldPassword The current password for verification.
 * @property newPassword The new desired password.
 * @property confirmPassword The confirmation of the new password.
 */
data class ChangePasswordRequest(
        @SerializedName("old_password") val oldPassword: String,
        @SerializedName("new_password") val newPassword: String,
        @SerializedName("confirm_password") val confirmPassword: String
)
