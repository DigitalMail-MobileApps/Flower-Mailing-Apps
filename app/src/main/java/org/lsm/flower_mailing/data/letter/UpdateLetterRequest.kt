package org.lsm.flower_mailing.data.letter

import com.google.gson.annotations.SerializedName

data class UpdateLetterRequest(
    @SerializedName("judul_surat")
    val judulSurat: String? = null,

    @SerializedName("pengirim")
    val pengirim: String? = null,

    @SerializedName("nomor_surat")
    val nomorSurat: String? = null,

    @SerializedName("nomor_agenda")
    val nomorAgenda: String? = null,

    @SerializedName("prioritas")
    val prioritas: String? = null,

    @SerializedName("tanggal_surat")
    val tanggalSurat: String? = null,

    @SerializedName("tanggal_masuk")
    val tanggalMasuk: String? = null,

    @SerializedName("isi_surat")
    val isiSurat: String? = null,

    @SerializedName("kesimpulan")
    val kesimpulan: String? = null,

    @SerializedName("disposisi")
    val disposisi: String? = null,

    @SerializedName("bidang_tujuan")
    val bidangTujuan: String? = null,

    @SerializedName("tanggal_disposisi")
    val tanggalDisposisi: String? = null,

    @SerializedName("jenis_surat")
    val jenisSurat: String? = null,

    @SerializedName("file_path")
    val filePath: String? = null,

    @SerializedName("status")
    val status: String? = null
)