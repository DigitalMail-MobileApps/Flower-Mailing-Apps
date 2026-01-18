package org.lsm.flower_mailing.data.model.request

import com.google.gson.annotations.SerializedName

/**
 * Payload for registering a new incoming letter (Surat Masuk).
 *
 * Used by Staff to log external letters received by the organization. These letters eventually
 * require a disposition from the Director.
 *
 * @property nomorSurat The original reference number from the sender.
 * @property pengirim The name of the external sender (person or organization).
 * @property judulSurat The subject or title of the received letter.
 * @property tanggalSurat The date written on the letter (YYYY-MM-DD).
 * @property tanggalMasuk The date the letter was received by the office (YYYY-MM-DD).
 * @property scope Usually "Eksternal".
 * @property fileScanPath The server-side path of the scanned document.
 * @property prioritas The urgency level (e.g., "biasa", "segera", "penting").
 * @property isiSurat A brief summary or transcript of the letter content.
 */
data class CreateIncomingLetterRequest(
        @SerializedName("nomor_surat") val nomorSurat: String,
        @SerializedName("pengirim") val pengirim: String,
        @SerializedName("judul_surat") val judulSurat: String,
        @SerializedName("tanggal_surat") val tanggalSurat: String,
        @SerializedName("tanggal_masuk") val tanggalMasuk: String,
        @SerializedName("scope") val scope: String,
        @SerializedName("file_scan_path") val fileScanPath: String,
        @SerializedName("prioritas") val prioritas: String,
        @SerializedName("isi_surat") val isiSurat: String
)
