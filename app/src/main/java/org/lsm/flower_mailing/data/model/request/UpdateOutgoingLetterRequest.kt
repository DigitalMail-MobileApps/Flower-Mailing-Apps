package org.lsm.flower_mailing.data.model.request

import com.google.gson.annotations.SerializedName

/**
 * Payload for updating an existing draft or revising a rejected outgoing letter.
 *
 * Only fields that are being modified need to be included, but generally the front-end sends the
 * full revised set of editable fields.
 *
 * @property judulSurat The updated subject of the letter.
 * @property isiSurat The updated content of the letter.
 * @property tujuan The updated recipient.
 * @property filePath The updated file path if the document was re-uploaded.
 */
data class UpdateOutgoingLetterRequest(
        @SerializedName("judul_surat") val judulSurat: String? = null,
        @SerializedName("isi_surat") val isiSurat: String? = null,
        @SerializedName("tujuan") val tujuan: String? = null,
        @SerializedName("file_path") val filePath: String? = null
)
