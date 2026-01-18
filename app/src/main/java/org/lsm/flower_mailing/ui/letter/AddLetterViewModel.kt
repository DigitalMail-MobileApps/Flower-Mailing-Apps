package org.lsm.flower_mailing.ui.letter

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.lsm.flower_mailing.data.UserPreferencesRepository
import org.lsm.flower_mailing.data.model.request.CreateIncomingLetterRequest
import org.lsm.flower_mailing.data.model.request.CreateOutgoingLetterRequest
import org.lsm.flower_mailing.data.model.response.VerifierDto
import org.lsm.flower_mailing.data.repository.IncomingLetterRepository
import org.lsm.flower_mailing.data.repository.OutgoingLetterRepository
import org.lsm.flower_mailing.remote.RetrofitClient

class AddLetterViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferencesRepository(application)

    // Manual injection
    private val outgoingRepo =
            OutgoingLetterRepository(
                    RetrofitClient.getOutgoingLetterApi(application),
                    RetrofitClient.getFileApi(application)
            )

    private val incomingRepo =
            IncomingLetterRepository(
                    RetrofitClient.getIncomingLetterApi(application),
                    RetrofitClient.getFileApi(application)
            )

    // --- State ---
    var userRole by mutableStateOf<String?>(null)
    var determinedJenisSurat by mutableStateOf("masuk")

    // Fields
    var pengirim by mutableStateOf("")
    var nomorSurat by mutableStateOf("")
    var nomorAgenda by mutableStateOf("")
    var prioritas by mutableStateOf("biasa")
    var tanggalSurat by mutableStateOf("")
    var tanggalMasuk by mutableStateOf("")
    var isiSurat by mutableStateOf("")
    var judulSurat by mutableStateOf("")
    var kesimpulan by
            mutableStateOf("") // Note: Not used in new strictly typed requests? check logic

    // For outgoing
    var tujuan by mutableStateOf("")
    var assignedVerifierId by mutableStateOf<Int?>(null) // Nullable until selected

    // List of Verifiers
    var verifiers by mutableStateOf<List<VerifierDto>>(emptyList())

    var fileUri by mutableStateOf<Uri?>(null)
    var fileName by mutableStateOf<String?>(null)

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private val _navigateBack = MutableSharedFlow<Boolean>()
    val navigateBack = _navigateBack.asSharedFlow()

    val prioritasOptions = listOf("biasa", "segera", "penting")

    init {
        viewModelScope.launch {
            userRole = userPreferences.userRoleFlow.first()
            // Kept existing logic for default tab, but architecture supports explicit choice now
            determinedJenisSurat =
                    if (userRole?.equals("adc", ignoreCase = true) == true) {
                        "keluar"
                    } else {
                        "masuk"
                    }

            // If outgoing, fetch verifiers
            fetchVerifiers()
        }
    }

    fun fetchVerifiers() {
        viewModelScope.launch {
            // Only fetch if needed (e.g. for Keluar or generally available)
            // Ideally we filter by scope if possible, e.g. "Internal" or just get all
            val result = outgoingRepo.getVerifiers()
            if (result.isSuccess) {
                verifiers = result.getOrNull() ?: emptyList()
                // Auto-select first if available?
                if (verifiers.isNotEmpty() && assignedVerifierId == null) {
                    assignedVerifierId = verifiers.first().id
                }
            } else {
                // Log error or show transient message?
                // errorMessage = "Gagal memuat verifikator: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun onFileSelected(uri: Uri?) {
        fileUri = uri
        fileName = if (uri != null) getFileName(uri) else null
    }

    fun createLetter(isDraft: Boolean) {
        if (!validateInput()) return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            var tempFile: File? = null

            try {
                if (fileUri == null) {
                    errorMessage = "File surat wajib diupload."
                    return@launch
                }

                tempFile = copyFileToCache(fileUri!!)
                if (tempFile == null) {
                    errorMessage = "Gagal memproses file."
                    return@launch
                }

                val requestFile = tempFile.asRequestBody("application/pdf".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

                val result =
                        if (determinedJenisSurat == "keluar") {
                            if (assignedVerifierId == null) {
                                errorMessage = "Pilih Verifikator terlebih dahulu."
                                return@launch
                            }

                            val request =
                                    CreateOutgoingLetterRequest(
                                            nomorSurat = nomorSurat,
                                            judulSurat = judulSurat,
                                            tujuan = tujuan, // Make sure UI has this field
                                            isiSurat = isiSurat,
                                            scope = "Internal", // Default or add UI for it
                                            assignedVerifierId = assignedVerifierId!!,
                                            filePath = "" // Will be filled by Repo
                                    )
                            outgoingRepo.createDraft(request, filePart)
                        } else {
                            val request =
                                    CreateIncomingLetterRequest(
                                            nomorSurat = nomorSurat,
                                            pengirim = pengirim,
                                            judulSurat = judulSurat,
                                            tanggalSurat = toUtcTimestamp(tanggalSurat),
                                            tanggalMasuk = toUtcTimestamp(tanggalMasuk),
                                            scope = "Eksternal",
                                            fileScanPath = "", // Will be filled by Repo
                                            prioritas = prioritas,
                                            isiSurat = isiSurat
                                    )
                            incomingRepo.registerLetter(request, filePart)
                        }

                result.onSuccess { _navigateBack.emit(true) }.onFailure { e ->
                    errorMessage = e.message ?: "Gagal membuat surat."
                }
            } catch (e: Exception) {
                errorMessage = e.message
                e.printStackTrace()
            } finally {
                isLoading = false
                tempFile?.delete()
            }
        }
    }

    // --- Helpers ---
    private fun validateInput(): Boolean {
        if (judulSurat.isBlank() || nomorSurat.isBlank()) {
            errorMessage = "Judul dan Nomor Surat wajib diisi."
            return false
        }
        return true
    }

    private fun getFileName(uri: Uri): String? {
        val context = getApplication<Application>()
        var name: String? = null
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) name = it.getString(index)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return name
    }

    private fun copyFileToCache(uri: Uri): File? {
        // ... (Keep existing implementation logic)
        val context = getApplication<Application>()
        val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.pdf")
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            }
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    // Kept for UI formatting
    fun formatMillisToDateTimeString(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(Date(millis))
    }

    private fun toUtcTimestamp(dateStr: String): String {
        // ... (Keep existing implementation or standard ISO)
        return if (dateStr.isNotBlank()) dateStr else getCurrentUtcTimestamp()
    }
    private fun getCurrentUtcTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US) // Simplify for now
        return sdf.format(Date())
    }
}
