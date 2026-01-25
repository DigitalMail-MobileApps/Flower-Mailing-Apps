package org.lsm.flower_mailing.data.model.request

import com.google.gson.annotations.SerializedName

data class UpdateIncomingLetterRequest(
        @SerializedName("nomor_surat") val nomorSurat: String? = null,
        @SerializedName("pengirim") val pengirim: String? = null,
        @SerializedName("judul_surat") val judulSurat: String? = null,
        @SerializedName("tanggal_surat") val tanggalSurat: String? = null, // String "2023-01-01"
        @SerializedName("tanggal_masuk") val tanggalMasuk: String? = null,
        @SerializedName("scope") val scope: String? = null, // UpdateMasuk HAS Scope
        @SerializedName("prioritas") val prioritas: String? = null,
        @SerializedName("isi_surat") val isiSurat: String? = null,
        @SerializedName("kesimpulan") val kesimpulan: String? = null,
        @SerializedName("status") val status: String? = null
)
