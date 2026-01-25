package org.lsm.flower_mailing.data.model.request

import com.google.gson.annotations.SerializedName

/**
 * Payload for adding a disposition to an incoming letter.
 *
 * This action is performed by the Director to instruct staff or subordinates on how to handle a
 * specific incoming letter.
 *
 * @property disposition The actual instruction text (e.g., "Process immediately").
 * @property dispositionUserIds List of User IDs who are assigned to handle this disposition.
 */
data class DispositionRequest(
        @SerializedName("instruksi_disposisi") val disposition: String,
        @SerializedName("tujuan_disposisi") val tujuanDisposisi: String,
        @SerializedName("catatan") val catatan: String = "",
        @SerializedName("needs_reply") val needsReply: Boolean = false
)
