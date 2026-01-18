package org.lsm.flower_mailing.data.model.request

import com.google.gson.annotations.SerializedName

/**
 * Payload for creating a new outgoing letter (Surat Keluar).
 *
 * This request initiates the drafting process for an internal letter. It requires a verifier to be
 * assigned so the letter can proceed to the verification stage once the draft is finalized.
 *
 * @property nomorSurat The official reference number for the letter (e.g., "SURAT/KELUAR/001").
 * @property judulSurat The subject or title of the letter.
 * @property tujuan The intended recipient of the letter (e.g., "Divisi IT").
 * @property isiSurat The partial or full content body of the letter.
 * @property scope The scope of the letter, typically "Internal" or "Eksternal".
 * @property assignedVerifierId The ID of the Manager who will verify this letter.
 * @property filePath The server-side path of the uploaded PDF file (assigned after upload).
 */
data class CreateOutgoingLetterRequest(
        @SerializedName("nomor_surat") val nomorSurat: String,
        @SerializedName("judul_surat") val judulSurat: String,
        @SerializedName("tujuan") val tujuan: String,
        @SerializedName("isi_surat") val isiSurat: String,
        @SerializedName("scope") val scope: String,
        @SerializedName("assigned_verifier_id") val assignedVerifierId: Int,
        @SerializedName("file_path") val filePath: String
)
