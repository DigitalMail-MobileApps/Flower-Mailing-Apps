package org.lsm.flower_mailing.data.letter

import com.google.gson.annotations.SerializedName

data class CreateLetterRequest(
    @SerializedName("pengirim")
    val pengirim: String,

    @SerializedName("nomor_surat")
    val nomorSurat: String,

    @SerializedName("nomor_agenda")
    val nomorAgenda: String,

    @SerializedName("disposisi")
    val disposisi: String,

    @SerializedName("tanggal_disposisi")
    val tanggalDisposisi: String, // Format: "2025-11-11T10:00:00Z"

    @SerializedName("bidang_tujuan")
    val bidangTujuan: String,

    @SerializedName("jenis_surat")
    val jenisSurat: String, // "masuk", "keluar", "internal"

    @SerializedName("prioritas")
    val prioritas: String?, // "biasa", "segera", "penting"

    @SerializedName("tanggal_surat")
    val tanggalSurat: String,

    @SerializedName("tanggal_masuk")
    val tanggalMasuk: String,

    @SerializedName("isi_surat")
    val isiSurat: String,

    @SerializedName("judul_surat")
    val judulSurat: String,

    @SerializedName("kesimpulan")
    val kesimpulan: String,

    @SerializedName("file_path")
    val filePath: String?,

    @SerializedName("status")
    val status: String?
)