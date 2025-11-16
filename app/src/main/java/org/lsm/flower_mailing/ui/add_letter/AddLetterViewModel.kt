package org.lsm.flower_mailing.ui.add_letter

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.lsm.flower_mailing.data.letter.CreateLetterRequest
import org.lsm.flower_mailing.remote.RetrofitClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AddLetterViewModel(application: Application) : AndroidViewModel(application) {

    private fun getCurrentUtcTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    var pengirim by mutableStateOf("")
    var nomorSurat by mutableStateOf("")
    var nomorAgenda by mutableStateOf("")
    var prioritas by mutableStateOf("biasa")
    var tanggalSurat by mutableStateOf("")
    var tanggalMasuk by mutableStateOf("")
    var isiSurat by mutableStateOf("")
    var judulSurat by mutableStateOf("")
    var kesimpulan by mutableStateOf("")
    var filePath by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private val _navigateBack = MutableSharedFlow<Boolean>()
    val navigateBack = _navigateBack.asSharedFlow()
    val prioritasOptions = listOf("biasa", "segera", "penting")

    fun formatMillisToDateTimeString(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(Date(millis))
    }
    private fun toUtcTimestamp(dateTimeString: String): String {
        if (dateTimeString.isBlank()) return getCurrentUtcTimestamp()
        return try {
            val localSdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            localSdf.timeZone = TimeZone.getDefault()
            val date = localSdf.parse(dateTimeString)

            val utcSdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            utcSdf.timeZone = TimeZone.getTimeZone("UTC")
            utcSdf.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            getCurrentUtcTimestamp()
        }
    }

    fun createLetter(isDraft: Boolean) {
        if (judulSurat.isBlank() || pengirim.isBlank() || nomorSurat.isBlank()) {
            errorMessage = "Judul, Pengirim, dan Nomor Surat tidak boleh kosong."
            return
        }
        if (tanggalSurat.isBlank() || tanggalMasuk.isBlank()) {
            errorMessage = "Tanggal Surat dan Tanggal Masuk tidak boleh kosong."
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val newStatus = if (isDraft) "draft" else "perlu_disposisi"
                val request = CreateLetterRequest(
                    pengirim = pengirim,
                    nomorSurat = nomorSurat,
                    nomorAgenda = nomorAgenda,
                    judulSurat = judulSurat,
                    isiSurat = isiSurat,
                    kesimpulan = kesimpulan,
                    prioritas = prioritas,
                    tanggalSurat = toUtcTimestamp(tanggalSurat),
                    tanggalMasuk = toUtcTimestamp(tanggalMasuk),
                    jenisSurat = "masuk",
                    status = newStatus,
                    disposisi = "",
                    tanggalDisposisi = getCurrentUtcTimestamp(),
                    bidangTujuan = "",
                    filePath = filePath
                )
                val response = RetrofitClient.getInstance(getApplication()).createLetter(request)

                if (response.message == "success") {
                    _navigateBack.emit(true)
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Terjadi kesalahan tidak diketahui"
            } finally {
                isLoading = false
            }
        }
    }
}