package org.lsm.flower_mailing.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * Common DTO for Letter Details (used by common get endpoint). Backend might unify this or we map
 * from specific responses. Assuming the backend 'common handler' return a structure compatible with
 * this.
 */
data class LetterDto(
        @SerializedName("ID") val id: Int = 0,
        @SerializedName("NomorSurat") val nomorSurat: String = "",
        @SerializedName("JudulSurat") val judulSurat: String = "",
        @SerializedName("Status") val status: String = "",
        // Common fields
        @SerializedName("Pengirim") val pengirim: String? = null,
        @SerializedName("IsiSurat") val isiSurat: String? = null,
        @SerializedName("Prioritas") val prioritas: String? = null,
        @SerializedName("CreatedAt") val createdAt: String = "",
        @SerializedName("FilePath") val filePath: String? = null,
        @SerializedName("scope") val scope: String? = null,

        // Surat Masuk specific
        @SerializedName("TanggalMasuk") val tanggalMasuk: String? = null,
        @SerializedName("TanggalSurat") val tanggalSurat: String? = null,

        // Surat Keluar specific
        @SerializedName("BidangTujuan") val bidangTujuan: String? = null,
        @SerializedName("NomorAgenda") val nomorAgenda: String? = null
)
