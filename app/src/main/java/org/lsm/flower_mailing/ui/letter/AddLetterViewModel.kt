package org.lsm.flower_mailing.ui.letter

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
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

class AddLetterViewModel(application: Application, private val savedStateHandle: SavedStateHandle) :
        AndroidViewModel(application) {

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

    var scope by mutableStateOf("Internal")

    init {
        viewModelScope.launch {
            userRole = userPreferences.userRoleFlow.first()
            // Kept existing logic for default tab, but architecture supports explicit choice now
            determinedJenisSurat =
                    if (userRole?.equals("staf_program", ignoreCase = true) == true) {
                        "keluar"
                    } else {
                        "masuk"
                    }

            // Check for reply arguments
            val replyToId = savedStateHandle.get<String>("replyToId")?.toIntOrNull()
            val replyToTitle = savedStateHandle.get<String>("replyToTitle")
            val replyToSender = savedStateHandle.get<String>("replyToSender")

            if (replyToId != null) {
                determinedJenisSurat = "keluar"
                // Determine scope based on role, just like new letters
                scope =
                        if (userRole?.equals("staf_program", ignoreCase = true) == true) {
                            "Eksternal"
                        } else {
                            "Internal"
                        }
                // Pre-fill
                judulSurat = if (replyToTitle != null) "Re: $replyToTitle" else ""
                // For reply, the destination (tujuan) is the original sender
                tujuan = replyToSender ?: ""

                // Store ID for request
                // We need a field for this
            } else {
                // Default scope logic
                if (userRole?.equals("staf_program", ignoreCase = true) == true) {
                    scope = "Eksternal"
                } else {
                    scope = "Internal"
                }
            }

            // If outgoing, fetch verifiers
            fetchVerifiers()
        }
    }

    // Helper to get the reply ID from savedState for the request
    private val inReplyToId: Int?
        get() = savedStateHandle.get<String>("replyToId")?.toIntOrNull()

    fun fetchVerifiers() {
        viewModelScope.launch {
            android.util.Log.d("AddLetterViewModel", "Fetching verifiers for scope: $scope")
            val result = outgoingRepo.getVerifiers(scope)
            if (result.isSuccess) {
                verifiers = result.getOrNull() ?: emptyList()
                // Auto-select first if available and nothing selected?
                // Actually keep it null so user must select, unless we want to default
            } else {
                android.util.Log.e(
                        "AddLetterViewModel",
                        "Failed to fetch verifiers: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    fun updateScope(newScope: String) {
        scope = newScope
        fetchVerifiers()
    }

    fun onFileSelected(uri: Uri?) {
        fileUri = uri
        fileName = if (uri != null) getFileName(uri) else null
    }

    fun onClearFile() {
        fileUri = null
        fileName = null
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

                val requestFile = tempFile.asRequestBody(getMimeType(tempFile).toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

                val result =
                        if (determinedJenisSurat == "keluar") {
                            // Fix: UI uses 'pengirim' field for the destination input in Outgoing
                            // mode.
                            val finalTujuan = if (tujuan.isBlank()) pengirim else tujuan

                            // Safety: Enforce Internal scope for Staf Lembaga (UI hides selector
                            // but let's be safe)
                            if (userRole?.equals("staf_lembaga", ignoreCase = true) == true) {
                                scope = "Internal"
                            }

                            if (finalTujuan.isBlank()) {
                                errorMessage = "Tujuan surat wajib diisi."
                                return@launch
                            }

                            // Backend auto-assigns verifier for "Internal" scope, so we don't
                            // strictly need one selected.
                            // For "Eksternal", verifier MUST be selected by user.

                            // Validation: Eksternal scope requires assigned verifier
                            val verifierId = assignedVerifierId ?: 18 // Default to 18 as requested
                            if (scope == "Eksternal" && verifierId == 0) {
                                errorMessage = "Verifikator wajib dipilih untuk surat Eksternal."
                                return@launch
                            }

                            val request =
                                    CreateOutgoingLetterRequest(
                                            nomorSurat = nomorSurat,
                                            pengirim = "Internal", // Sender is always internal org
                                            judulSurat = judulSurat,
                                            tujuan = finalTujuan,
                                            isiSurat = isiSurat,
                                            scope = scope,
                                            jenisSurat = "keluar",
                                            assignedVerifierId = verifierId,
                                            filePath = "", // Will be handled by Repo
                                            status = if (isDraft) "draft" else "perlu_verifikasi",
                                            nomorAgenda = nomorAgenda,
                                            tanggalSurat = toUtcTimestamp(tanggalSurat),
                                            tanggalMasuk = toUtcTimestamp(tanggalMasuk),
                                            kesimpulan = kesimpulan,
                                            inReplyToId = inReplyToId
                                    )
                            android.util.Log.d(
                                    "AddLetterViewModel",
                                    "Creating outgoing draft - nomorAgenda: '$nomorAgenda', kesimpulan: '$kesimpulan', tanggalSurat: '${request.tanggalSurat}', tanggalMasuk: '${request.tanggalMasuk}'"
                            )
                            outgoingRepo.createDraft(request, filePart)
                        } else {
                            android.util.Log.d("AddLetterViewModel", "Registering incoming letter")
                            val request =
                                    CreateIncomingLetterRequest(
                                            nomorSurat = nomorSurat,
                                            pengirim = pengirim,
                                            judulSurat = judulSurat,
                                            tanggalSurat = toUtcTimestamp(tanggalSurat),
                                            tanggalMasuk = toUtcTimestamp(tanggalMasuk),
                                            scope = scope,
                                            fileScanPath = "",
                                            prioritas = prioritas,
                                            isiSurat = isiSurat,
                                            kesimpulan = kesimpulan,
                                            status = if (isDraft) "draft" else "belum_disposisi"
                                    )
                            incomingRepo.registerLetter(request, filePart)
                        }

                result
                        .onSuccess {
                            android.util.Log.d("AddLetterViewModel", "Success")
                            _navigateBack.emit(true)
                        }
                        .onFailure { e ->
                            android.util.Log.e("AddLetterViewModel", "Error: ${e.message}", e)
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
        val context = getApplication<Application>()
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "application/pdf"
        val extension =
                when {
                    mimeType.contains("pdf") -> "pdf"
                    mimeType.contains("image/jpeg") -> "jpg"
                    mimeType.contains("image/png") -> "png"
                    else -> "pdf" // Default fallback
                }

        val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.$extension")
        return try {
            contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            }
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    private fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "pdf" -> "application/pdf"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            else -> "application/octet-stream"
        }
    }

    // Kept for UI formatting
    fun formatMillisToDateTimeString(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(Date(millis))
    }

    /**
     * Convert date string to YYYY-MM-DD format for backend compatibility. Falls back to current
     * date if input is blank or invalid.
     */
    private fun toDateOnlyString(dateStr: String): String {
        if (dateStr.isBlank()) return getCurrentDateOnly()

        return try {
            // If already in YYYY-MM-DD format, return as-is
            if (dateStr.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                dateStr
            } else {
                // Try to parse and format
                val inputSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val date = inputSdf.parse(dateStr)
                if (date != null) {
                    inputSdf.format(date)
                } else {
                    getCurrentDateOnly()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            getCurrentDateOnly()
        }
    }

    private fun getCurrentDateOnly(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(Date())
    }

    // Updated for backend ISO8601 compatibility
    private fun toUtcTimestamp(dateStr: String): String {
        if (dateStr.isBlank()) return getCurrentUtcTimestamp()

        return try {
            val inputSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = inputSdf.parse(dateStr)
            if (date != null) {
                val outputSdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                outputSdf.timeZone = TimeZone.getTimeZone("UTC")
                outputSdf.format(date)
            } else {
                getCurrentUtcTimestamp()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            getCurrentUtcTimestamp()
        }
    }
    private fun getCurrentUtcTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }
}
