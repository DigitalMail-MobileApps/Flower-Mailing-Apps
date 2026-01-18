

package org.lsm.flower_mailing.data.model.response

import com.google.gson.annotations.SerializedName

/** Represents an Outgoing Letter (Surat Keluar) as returned by the API. */
data class OutgoingLetterDto(
        @SerializedName("id") val id: Int,
        @SerializedName("nomor_surat") val nomorSurat: String,
        @SerializedName("judul_surat") val judulSurat: String,
        @SerializedName("status") val status: String,
        @SerializedName("tujuan") val tujuan: String,
        @SerializedName("created_at") val createdAt: String,
        @SerializedName("tanggal_surat") val tanggalSurat: String?,
        @SerializedName("file_path") val filePath: String?
)

/** Represents an Incoming Letter (Surat Masuk) as returned by the API. */
data class IncomingLetterDto(
        @SerializedName("id") val id: Int,
        @SerializedName("nomor_surat") val nomorSurat: String,
        @SerializedName("judul_surat") val judulSurat: String,
        @SerializedName("status") val status: String,
        @SerializedName("pengirim") val pengirim: String,
        @SerializedName("tanggal_masuk") val tanggalMasuk: String,
        @SerializedName("disposisi") val disposisi: String?,
        @SerializedName("tanggal_surat") val tanggalSurat: String?,
        @SerializedName("prioritas") val prioritas: String?,
        @SerializedName("isi_surat") val isiSurat: String?,
        @SerializedName("file_scan_path") val fileScanPath: String?
)

/** Wrapper for paginated list responses. */
data class PaginatedResponse<T>(
        @SerializedName("data") val items: List<T>,
        @SerializedName("meta") val meta: MetaDto
)

data class MetaDto(
        @SerializedName("page") val page: Int,
        @SerializedName("limit") val limit: Int,
        @SerializedName("total") val total: Int
)
