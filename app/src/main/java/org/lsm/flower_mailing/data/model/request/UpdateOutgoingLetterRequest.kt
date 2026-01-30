package org.lsm.flower_mailing.data.model.request

import com.google.gson.annotations.SerializedName

data class UpdateOutgoingLetterRequest(
        @SerializedName("pengirim") val pengirim: String? = null,
        @SerializedName("nomor_surat") val nomorSurat: String? = null,
        @SerializedName("nomor_agenda") val nomorAgenda: String? = null,
        @SerializedName("judul_surat") val judulSurat: String? = null,
        @SerializedName("bidang_tujuan") val tujuan: String? = null,
        @SerializedName("isi_surat") val isiSurat: String? = null,
        @SerializedName("prioritas") val prioritas: String? = null,
        @SerializedName("kesimpulan") val kesimpulan: String? = null,
        @SerializedName("tanggal_surat") val tanggalSurat: String? = null,
        @SerializedName("tanggal_masuk") val tanggalMasuk: String? = null,
        @SerializedName("status") val status: String? = null
)
