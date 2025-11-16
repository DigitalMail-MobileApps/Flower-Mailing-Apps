package org.lsm.flower_mailing.ui.letter_detail

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.lsm.flower_mailing.data.UserPreferencesRepository
import org.lsm.flower_mailing.data.letter.CreateLetterRequest
import org.lsm.flower_mailing.data.letter.Letter
import org.lsm.flower_mailing.remote.RetrofitClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class LetterUiState(
    val isLoading: Boolean = true,
    val isLetterInfoEditable: Boolean = false,
    val isDispositionSectionVisible: Boolean = false,
    val isDispositionInfoEditable: Boolean = false,
    val buttons: List<LetterButtonType> = emptyList(),
    val errorMessage: String? = null
)

// This defines which buttons to show
enum class LetterButtonType {
    SAVE_DRAFT,
    SUBMIT_TO_ADC,
    SUBMIT_DISPOSITION
}

class LetterDetailViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = UserPreferencesRepository(application)
    private val api = RetrofitClient.getInstance(application)

    // Letter ID from navigation
    private val letterId: Int = savedStateHandle.get<String>("letterId")?.toIntOrNull() ?: 0

    // Original letter, fetched from API
    private var originalLetter: Letter? = null

    // --- FORM STATE ---
    // These hold the editable values for the form.
    // We use mutableStateOf for compatibility with Compose.
    var judulSurat by mutableStateOf("")
    var pengirim by mutableStateOf("")
    var nomorSurat by mutableStateOf("")
    var nomorAgenda by mutableStateOf("")
    var prioritas by mutableStateOf("biasa")
    var tanggalSurat by mutableStateOf("")
    var tanggalMasuk by mutableStateOf("")
    var isiSurat by mutableStateOf("")
    var kesimpulan by mutableStateOf("")
    // Disposition fields
    var disposisi by mutableStateOf("")
    var bidangTujuan by mutableStateOf("")
    var tanggalDisposisi by mutableStateOf("")

    val prioritasOptions = listOf("biasa", "segera", "penting")

    // --- UI STATE ---
    private val _uiState = MutableStateFlow(LetterUiState())
    val uiState = _uiState.asStateFlow()

    // --- NAVIGATION ---
    private val _navigateBack = MutableSharedFlow<Boolean>()
    val navigateBack = _navigateBack.asSharedFlow()

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            _uiState.value = LetterUiState(isLoading = true) // Start loading
            try {
                val userRole = repository.userRoleFlow.first()
                val response = api.getLetterById(letterId)

                if (response.status == "success") {
                    val letter = response.data
                    originalLetter = letter

                    // 1. Populate all the form fields
                    populateFormFields(letter)

                    // 2. Calculate what the UI should show
                    calculateUiMode(userRole, letter.status)
                } else {
                    _uiState.value = LetterUiState(isLoading = false, errorMessage = response.message)
                }
            } catch (e: Exception) {
                _uiState.value = LetterUiState(isLoading = false, errorMessage = e.message ?: "Unknown error")
            }
        }
    }

    private fun populateFormFields(letter: Letter) {
        judulSurat = letter.judulSurat
        pengirim = letter.pengirim
        nomorSurat = letter.nomorSurat
        nomorAgenda = letter.nomorSurat // Assuming, or add nomorAgenda to Letter.kt
        prioritas = letter.prioritas ?: "biasa"
        tanggalSurat = formatTimestampToDateTime(letter.tanggalSurat)
        tanggalMasuk = formatTimestampToDateTime(letter.tanggalMasuk)
        isiSurat = letter.isiSurat ?: ""
        kesimpulan = "" // Add 'kesimpulan' to Letter.kt if it exists

        // Populate disposition fields
        disposisi = "" // Add 'disposisi' to Letter.kt if it exists
        bidangTujuan = "" // Add 'bidangTujuan' to Letter.kt if it exists
        tanggalDisposisi = formatTimestampToDateTime(getCurrentUtcTimestamp()) // Default to now
    }

    private fun calculateUiMode(role: String?, status: String) {
        // This implements the logic table we discussed
        val newState = when (role) {
            "bagian_umum" -> when (status) {
                "draft" -> LetterUiState( // "Bagian Umum" editing a draft
                    isLoading = false,
                    isLetterInfoEditable = true,
                    isDispositionSectionVisible = false,
                    isDispositionInfoEditable = false,
                    buttons = listOf(LetterButtonType.SAVE_DRAFT, LetterButtonType.SUBMIT_TO_ADC)
                )
                else -> LetterUiState( // "Bagian Umum" viewing a live letter
                    isLoading = false,
                    isLetterInfoEditable = false,
                    isDispositionSectionVisible = true,
                    isDispositionInfoEditable = false, // Read-only
                    buttons = emptyList()
                )
            }
            "adc", "direktur" -> when (status) {
                "perlu_disposisi" -> LetterUiState( // "ADC" doing a disposition
                    isLoading = false,
                    isLetterInfoEditable = false, // Read-only
                    isDispositionSectionVisible = true,
                    isDispositionInfoEditable = true, // EDITABLE
                    buttons = listOf(LetterButtonType.SUBMIT_DISPOSITION)
                )
                else -> LetterUiState( // "ADC" viewing a draft or history
                    isLoading = false,
                    isLetterInfoEditable = false,
                    isDispositionSectionVisible = true,
                    isDispositionInfoEditable = false, // Read-only
                    buttons = emptyList()
                )
            }
            else -> LetterUiState(isLoading = false, errorMessage = "Unknown user role")
        }
        _uiState.value = newState
    }

    fun onSaveDraft() {
        submitChanges("draft")
    }

    fun onSubmitToAdc() {
        submitChanges("perlu_disposisi")
    }

    fun onSubmitDisposition() {
        submitChanges("sudah_disposisi")
    }

    private fun submitChanges(newStatus: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val original = originalLetter ?: return@launch
            val request = CreateLetterRequest(
                judulSurat = judulSurat,
                pengirim = pengirim,
                nomorSurat = nomorSurat,
                nomorAgenda = nomorAgenda,
                prioritas = prioritas,
                tanggalSurat = toUtcTimestamp(tanggalSurat),
                tanggalMasuk = toUtcTimestamp(tanggalMasuk),
                isiSurat = isiSurat,
                kesimpulan = kesimpulan,
                disposisi = disposisi,
                bidangTujuan = bidangTujuan,
                tanggalDisposisi = toUtcTimestamp(tanggalDisposisi),
                jenisSurat = original.jenisSurat,
                filePath = originalLetter?.isiSurat ?: "",
                status = newStatus
            )

            try {
                val response = api.updateLetter(letterId, request)
                if (response.status == "success") {
                    _navigateBack.emit(true) // Success, go back
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = response.message)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    private fun getCurrentUtcTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    fun formatTimestampToDateTime(timestamp: String?): String {
        if (timestamp.isNullOrBlank()) return ""
        return try {
            val utcSdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            utcSdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = utcSdf.parse(timestamp)

            val localSdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            localSdf.timeZone = TimeZone.getDefault()
            localSdf.format(date)
        } catch (e: Exception) {
            timestamp.take(10)
        }
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
            try { "${dateTimeString}T00:00:00Z" }
            catch (e2: Exception) { getCurrentUtcTimestamp() }
        }
    }
}