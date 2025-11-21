package org.lsm.flower_mailing.ui.letter

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.lsm.flower_mailing.data.UserPreferencesRepository
import org.lsm.flower_mailing.data.letter.CreateLetterRequest
import org.lsm.flower_mailing.remote.RetrofitClient
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AddLetterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserPreferencesRepository(application)

    // Helper to get current UTC timestamp
    private fun getCurrentUtcTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    // --- Role & Type State ---
    var userRole by mutableStateOf<String?>(null)
    // This will be automatically set based on role
    var determinedJenisSurat by mutableStateOf("masuk")

    // --- Fields ---
    var pengirim by mutableStateOf("")
    var nomorSurat by mutableStateOf("")
    var nomorAgenda by mutableStateOf("")
    var prioritas by mutableStateOf("biasa")
    var tanggalSurat by mutableStateOf("")
    var tanggalMasuk by mutableStateOf("")
    var isiSurat by mutableStateOf("")
    var judulSurat by mutableStateOf("")
    var kesimpulan by mutableStateOf("")

    var fileUri by mutableStateOf<Uri?>(null)
    var fileName by mutableStateOf<String?>(null)

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private val _navigateBack = MutableSharedFlow<Boolean>()
    val navigateBack = _navigateBack.asSharedFlow()

    val prioritasOptions = listOf("biasa", "segera", "penting")

    init {
        viewModelScope.launch {
            userRole = repository.userRoleFlow.first()
            determinedJenisSurat = if (userRole?.equals("adc", ignoreCase = true) == true) {
                "keluar"
            } else {
                "masuk"
            }
        }
    }

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

    fun onFileSelected(uri: Uri?) {
        fileUri = uri
        fileName = if (uri != null) getFileName(uri) else null
    }
    private fun getFileName(uri: Uri): String? {
        val context = getApplication<Application>()
        var name: String? = null
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) name = it.getString(nameIndex)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            name = "file_${System.currentTimeMillis()}"
        }
        return name
    }
    private fun copyFileToCache(uri: Uri): File? {
        val context = getApplication<Application>()
        val contentResolver = context.contentResolver
        val tempFile: File
        try {
            val tempFileName = getFileName(uri) ?: "upload_${System.currentTimeMillis()}"
            tempFile = File(context.cacheDir, tempFileName)
            contentResolver.openInputStream(uri)?.use { inputStream ->
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return tempFile
    }

    fun createLetter(isDraft: Boolean) {
        if (judulSurat.isBlank() || pengirim.isBlank() || nomorSurat.isBlank()) {
            errorMessage = "Judul, Pengirim, dan Nomor Surat tidak boleh kosong."
            return
        }
        if (tanggalSurat.isBlank()) {
            errorMessage = "Tanggal Surat tidak boleh kosong."
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            var tempFile: File? = null

            try {
                val newStatus = if (isDraft) {
                    "draft"
                } else {
                    if (determinedJenisSurat == "keluar") {
                        "perlu_persetujuan"
                    } else {
                        "perlu_verifikasi"
                    }
                }

                val letterMetadata = CreateLetterRequest(
                    pengirim = pengirim,
                    nomorSurat = nomorSurat,
                    nomorAgenda = nomorAgenda,
                    judulSurat = judulSurat,
                    isiSurat = isiSurat,
                    kesimpulan = kesimpulan,
                    prioritas = prioritas,
                    tanggalSurat = toUtcTimestamp(tanggalSurat),
                    tanggalMasuk = if (determinedJenisSurat == "masuk") toUtcTimestamp(tanggalMasuk) else getCurrentUtcTimestamp(), // tanggalMasuk only relevant for incoming

                    jenisSurat = determinedJenisSurat, // Auto-set based on role
                    status = newStatus,

                    disposisi = "",
                    tanggalDisposisi = getCurrentUtcTimestamp(),
                    bidangTujuan = "",
                    filePath = null
                )

                val dataJson = Gson().toJson(letterMetadata)
                val dataRequestBody = dataJson.toRequestBody("text/plain".toMediaTypeOrNull())
                var filePart: MultipartBody.Part? = null
                fileUri?.let { uri ->
                    val context = getApplication<Application>()
                    tempFile = copyFileToCache(uri)

                    if (tempFile == null) {
                        errorMessage = "Tidak dapat memproses file yang dipilih."
                        isLoading = false
                        return@launch
                    }

                    val fileMimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
                    val requestFile = tempFile!!.asRequestBody(fileMimeType.toMediaTypeOrNull())

                    filePart = MultipartBody.Part.createFormData(
                        "file_surat",
                        fileName ?: tempFile!!.name,
                        requestFile
                    )
                }

                val response = RetrofitClient.getInstance(getApplication()).createLetter(
                    data = dataRequestBody,
                    file_surat = filePart
                )

                if (response.message == "success") {
                    _navigateBack.emit(true)
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Terjadi kesalahan tidak diketahui"
                e.printStackTrace()
            } finally {
                isLoading = false
                tempFile?.delete()
            }
        }
    }
}