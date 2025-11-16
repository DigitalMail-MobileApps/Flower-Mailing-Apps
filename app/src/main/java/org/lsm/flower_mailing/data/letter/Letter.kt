package org.lsm.flower_mailing.data.letter

import com.google.gson.annotations.SerializedName

data class Letter(
    @SerializedName("id_surat")
    val id: Int,

    @SerializedName("judul_surat")
    val judulSurat: String,

    @SerializedName("pengirim")
    val pengirim: String,

    @SerializedName("nomor_surat")
    val nomorSurat: String,

    @SerializedName("status")
    val status: String,

    @SerializedName ("tanggal_surat")
    val tanggalSurat: String,

    @SerializedName("tanggal_masuk")
    val tanggalMasuk: String,

    @SerializedName("jenis_surat")
    val jenisSurat: String,

    @SerializedName("prioritas")
    val prioritas: String?,

    @SerializedName("isi_surat")
    val isiSurat: String?
)

data class LetterDataPayload(
    @SerializedName("items")
    val items: List<Letter>,

    @SerializedName("meta")
    val meta: Meta
)

data class Meta(
    @SerializedName("page")
    val page: Int,

    @SerializedName("limit")
    val limit: Int,

    @SerializedName("total")
    val total: Int
)