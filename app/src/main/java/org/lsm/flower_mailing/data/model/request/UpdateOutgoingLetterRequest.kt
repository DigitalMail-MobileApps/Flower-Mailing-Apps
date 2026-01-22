package org.lsm.flower_mailing.data.model.request

import com.google.gson.annotations.SerializedName

data class UpdateOutgoingLetterRequest(
        @SerializedName("pengirim") val pengirim: String? = null,
        @SerializedName("nomor_surat") val nomorSurat: String? = null,
        @SerializedName("judul_surat") val judulSurat: String? = null,
        @SerializedName("bidang_tujuan")
        val tujuan: String? = null, // Backend uses 'bidang_tujuan' mapped to struct 'BidangTujuan'
        @SerializedName("isi_surat") val isiSurat: String? = null,
        @SerializedName("scope")
        val scope: String? =
                null, // Backend doesn't explicitly list scope in Update struct shown but might be
        // needed or handled via other fields? Wait, lines 44-60 of request_keluar.go
        // didn't show 'scope'.
        // Let me re-read request_keluar.go carefully.
        // It has: Pengirim, NomorSurat, NomorAgenda, Disposisi, TanggalDisposisi, BidangTujuan,
        // JenisSurat, Prioritas, IsiSurat, TanggalSurat, TanggalMasuk, JudulSurat, Kesimpulan,
        // FilePath, Status.
        // DOES NOT HAVE SCOPE.
        // It has AssignedVerifierID in separate struct `CreateLetterKeluarRequest` maybe?
        // Update struct lines 44-60:
        // AssignedVerifierID is NOT in UpdateLetterKeluarRequest shown in snippet.
        // This means we might NOT be able to change Verifier/Scope during edit?
        // Or maybe it was cut off?
        // Let me check if I need to add more fields based on what's available.
        @SerializedName("prioritas") val prioritas: String? = null,
        @SerializedName("kesimpulan") val kesimpulan: String? = null
)
