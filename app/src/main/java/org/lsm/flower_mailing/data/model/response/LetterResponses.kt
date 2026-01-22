package org.lsm.flower_mailing.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * Represents an Outgoing Letter (Surat Keluar) as returned by the API. Backend uses GORM model
 * which serializes with PascalCase field names.
 */
data class OutgoingLetterDto(
        @SerializedName("ID") val id: Int = 0,
        @SerializedName("NomorSurat") val nomorSurat: String = "",
        @SerializedName("JudulSurat") val judulSurat: String = "",
        @SerializedName("Status") val status: String = "",
        @SerializedName("BidangTujuan") val tujuan: String = "",
        @SerializedName("CreatedAt") val createdAt: String = "",
        @SerializedName("TanggalSurat") val tanggalSurat: String? = null,
        @SerializedName("FilePath") val filePath: String? = null,
        @SerializedName("Pengirim") val pengirim: String? = null,
        @SerializedName("JenisSurat") val jenisSurat: String? = null,
        @SerializedName("scope") val scope: String? = null, // lowercase in User model
        @SerializedName("Prioritas") val prioritas: String? = null,
        @SerializedName("IsiSurat") val isiSurat: String? = null,
        @SerializedName("NomorAgenda") val nomorAgenda: String? = null,
        @SerializedName("TanggalMasuk") val tanggalMasuk: String? = null
)

/**
 * Represents an Incoming Letter (Surat Masuk) as returned by the API. Backend uses GORM model which
 * serializes with PascalCase field names.
 */
data class IncomingLetterDto(
        @SerializedName("ID") val id: Int = 0,
        @SerializedName("NomorSurat") val nomorSurat: String = "",
        @SerializedName("JudulSurat") val judulSurat: String = "",
        @SerializedName("Status") val status: String = "",
        @SerializedName("Pengirim") val pengirim: String = "",
        @SerializedName("TanggalMasuk") val tanggalMasuk: String? = null,
        @SerializedName("Disposisi") val disposisi: String? = null,
        @SerializedName("TanggalSurat") val tanggalSurat: String? = null,
        @SerializedName("Prioritas") val prioritas: String? = null,
        @SerializedName("IsiSurat") val isiSurat: String? = null,
        @SerializedName("FilePath") val fileScanPath: String? = null,
        @SerializedName("CreatedAt") val createdAt: String? = null
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
