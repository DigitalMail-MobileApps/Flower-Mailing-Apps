package org.lsm.flower_mailing.data.letter

import com.google.gson.annotations.SerializedName

/**
 * Letter data class that matches backend GORM model JSON serialization. GORM uses PascalCase for
 * field names in JSON output.
 */
data class Letter(
        @SerializedName("ID") val id: Int = 0,
        @SerializedName("JudulSurat") val judulSurat: String = "",
        @SerializedName("Pengirim") val pengirim: String = "",
        @SerializedName("NomorSurat") val nomorSurat: String = "",
        @SerializedName("Status") val status: String = "",
        @SerializedName("TanggalSurat") val tanggalSurat: String? = null,
        @SerializedName("TanggalMasuk") val tanggalMasuk: String? = null,
        @SerializedName("JenisSurat") val jenisSurat: String = "",
        @SerializedName("Prioritas") val prioritas: String? = null,
        @SerializedName("IsiSurat") val isiSurat: String? = null,
        @SerializedName("NomorAgenda") val nomorAgenda: String? = null,
        @SerializedName("Disposisi") val disposisi: String? = null,
        @SerializedName("TanggalDisposisi") val tanggalDisposisi: String? = null,
        @SerializedName("BidangTujuan") val bidangTujuan: String? = null,
        @SerializedName("Kesimpulan") val kesimpulan: String? = null,
        @SerializedName("FilePath") val filePath: String? = null,
        @SerializedName("CreatedAt") val createdAt: String? = null,
        @SerializedName("scope") // Explicit lowercase json tag in backend
        val scope: String? = null,
        @SerializedName("needs_reply") val needsReply: Boolean = false
)

data class LetterDataPayload(
        @SerializedName("items") val items: List<Letter>,
        @SerializedName("meta") val meta: Meta
)

data class Meta(
        @SerializedName("page") val page: Int,
        @SerializedName("limit") val limit: Int,
        @SerializedName("total") val total: Int
)
