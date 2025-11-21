package org.lsm.flower_mailing.ui.letter

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
import org.lsm.flower_mailing.data.letter.Letter
import org.lsm.flower_mailing.data.letter.UpdateLetterRequest
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
    val downloadUrl: String? = null,
    val buttons: List<LetterButtonType> = emptyList(),
    val errorMessage: String? = null
)
enum class LetterButtonType {
    // Flow surat masuk
    SAVE_DRAFT,
    SUBMIT_TO_ADC,
    VERIFY_AND_FORWARD,
    SUBMIT_DISPOSITION,

    // Flow surat keluar
    AJUKAN_PERSETUJUAN,
    APPROVE_LETTER,
    REJECT_REVISION,
    FINALIZE_SEND
}

class LetterDetailViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = UserPreferencesRepository(application)
    private val api = RetrofitClient.getInstance(application)
    private val letterId: String = savedStateHandle.get<String>("letterId") ?: "0"
    private var originalLetter: Letter? = null

    var judulSurat by mutableStateOf("")
    var pengirim by mutableStateOf("")
    var nomorSurat by mutableStateOf("")
    var nomorAgenda by mutableStateOf("")
    var prioritas by mutableStateOf("biasa")
    var tanggalSurat by mutableStateOf("")
    var tanggalMasuk by mutableStateOf("")
    var isiSurat by mutableStateOf("")
    var kesimpulan by mutableStateOf("")
    var disposisi by mutableStateOf("")
    var bidangTujuan by mutableStateOf("")
    var tanggalDisposisi by mutableStateOf("")

    val prioritasOptions = listOf("biasa", "segera", "penting")

    private val _uiState = MutableStateFlow(LetterUiState())
    val uiState = _uiState.asStateFlow()
    private val _navigateBack = MutableSharedFlow<Boolean>()
    val navigateBack = _navigateBack.asSharedFlow()

    init {
        fetchLetterDetails()
    }

    fun fetchLetterDetails() {
        viewModelScope.launch {
            _uiState.value = LetterUiState(isLoading = true)
            try {
                val userRole = repository.userRoleFlow.first()
                val response = api.getLetterById(letterId)
                if (response.status == "success") {
                    val letter = response.data
                    originalLetter = letter
                    populateFormFields(letter)
                    calculateUiState(userRole, letter)
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
        nomorAgenda = letter.nomorAgenda
        prioritas = letter.prioritas ?: "biasa"
        tanggalSurat = formatTimestampToDateTime(letter.tanggalSurat)
        tanggalMasuk = formatTimestampToDateTime(letter.tanggalMasuk)
        isiSurat = letter.isiSurat ?: ""
        kesimpulan = letter.kesimpulan ?: ""
        disposisi = letter.disposisi ?: ""
        bidangTujuan = letter.bidangTujuan ?: ""

        val status = letter.status
        val isActionNeeded = status == "perlu_verifikasi" || status == "belum_disposisi" || status == "perlu_persetujuan"

        tanggalDisposisi = if (isActionNeeded)
            formatMillisToDateTimeString(System.currentTimeMillis())
        else
            formatTimestampToDateTime(letter.tanggalDisposisi)
    }

    private fun calculateUiState(role: String?, letter: Letter) {
        val status = letter.status
        val type = letter.jenisSurat
        if (type == "keluar") {
            calculateUiStateSuratKeluar(role, status, letter)
        } else {
            calculateUiStateSuratMasuk(role, status, letter)
        }
    }

    private fun calculateUiStateSuratKeluar(role: String?, status: String, letter: Letter) {
        val newState = when {
            role.equals("adc", ignoreCase = true) -> when (status) {
                "draft" -> LetterUiState(
                    isLoading = false,
                    isLetterInfoEditable = true,
                    downloadUrl = letter.filePath,
                    buttons = listOf(LetterButtonType.SAVE_DRAFT, LetterButtonType.AJUKAN_PERSETUJUAN)
                )
                "perlu_revisi" -> LetterUiState(
                    isLoading = false,
                    isLetterInfoEditable = true,
                    isDispositionSectionVisible = false,
                    isDispositionInfoEditable = false,
                    downloadUrl = letter.filePath,
                    buttons = listOf(LetterButtonType.SAVE_DRAFT, LetterButtonType.AJUKAN_PERSETUJUAN)
                )
                "disetujui" -> LetterUiState(
                    isLoading = false,
                    isLetterInfoEditable = false,
                    isDispositionSectionVisible = false,
                    isDispositionInfoEditable = false,
                    downloadUrl = letter.filePath,
                    buttons = listOf(LetterButtonType.FINALIZE_SEND)
                )
                else -> LetterUiState(
                    isLoading = false,
                    isLetterInfoEditable = false,
                    downloadUrl = letter.filePath,
                    buttons = emptyList()
                )
            }
            role.equals("direktur", ignoreCase = true) -> when (status) {
                "perlu_persetujuan" -> LetterUiState(
                    isLoading = false,
                    isLetterInfoEditable = false,
                    isDispositionSectionVisible = true,
                    isDispositionInfoEditable = true,
                    downloadUrl = letter.filePath,
                    buttons = listOf(LetterButtonType.APPROVE_LETTER, LetterButtonType.REJECT_REVISION)
                )
                else -> LetterUiState(
                    isLoading = false,
                    isLetterInfoEditable = false,
                    isDispositionSectionVisible = true,
                    isDispositionInfoEditable = false,
                    downloadUrl = letter.filePath,
                    buttons = emptyList()
                )
            }
            else -> LetterUiState(isLoading = false)
        }
        _uiState.value = newState
    }

    private fun calculateUiStateSuratMasuk(role: String?, status: String, letter: Letter) {
        val newState = when {
            role.equals("bagian_umum", ignoreCase = true) -> when (status) {
                "draft" -> LetterUiState(
                    isLoading = false,
                    isLetterInfoEditable = true,
                    downloadUrl = letter.filePath,
                    buttons = listOf(LetterButtonType.SAVE_DRAFT, LetterButtonType.SUBMIT_TO_ADC)
                )
                else -> LetterUiState(
                    isLoading = false,
                    isLetterInfoEditable = false,
                    isDispositionSectionVisible = true,
                    isDispositionInfoEditable = false,
                    downloadUrl = letter.filePath,
                    buttons = emptyList()
                )
            }
            role.equals("adc", ignoreCase = true) -> when (status) {
                "perlu_verifikasi" -> LetterUiState(
                    isLoading = false,
                    isLetterInfoEditable = true,
                    downloadUrl = letter.filePath,
                    buttons = listOf(LetterButtonType.VERIFY_AND_FORWARD)
                )
                else -> LetterUiState(
                    isLoading = false,
                    isLetterInfoEditable = false,
                    isDispositionSectionVisible = true,
                    isDispositionInfoEditable = false,
                    downloadUrl = letter.filePath,
                    buttons = emptyList()
                )
            }
            role.equals("direktur", ignoreCase = true) -> when (status) {
                "belum_disposisi" -> LetterUiState(
                    isLoading = false,
                    isLetterInfoEditable = false,
                    isDispositionSectionVisible = true,
                    isDispositionInfoEditable = true,
                    downloadUrl = letter.filePath,
                    buttons = listOf(LetterButtonType.SUBMIT_DISPOSITION)
                )
                else -> LetterUiState(
                    isLoading = false,
                    isLetterInfoEditable = false,
                    isDispositionSectionVisible = true,
                    isDispositionInfoEditable = false,
                    downloadUrl = letter.filePath,
                    buttons = emptyList()
                )
            }
            else -> LetterUiState(isLoading = false, errorMessage = "Unknown user role")
        }
        _uiState.value = newState
    }

    fun onSaveDraft() {
        submitUpdate(UpdateLetterRequest(
            judulSurat = judulSurat, pengirim = pengirim, nomorSurat = nomorSurat,
            nomorAgenda = nomorAgenda, prioritas = prioritas,
            tanggalSurat = toUtcTimestamp(tanggalSurat),
            isiSurat = isiSurat, kesimpulan = kesimpulan,
            status = "draft"
        ))
    }

    fun onSubmitToAdc() {
        submitUpdate(UpdateLetterRequest(
            judulSurat = judulSurat, pengirim = pengirim, nomorSurat = nomorSurat,
            nomorAgenda = nomorAgenda, prioritas = prioritas,
            tanggalSurat = toUtcTimestamp(tanggalSurat), tanggalMasuk = toUtcTimestamp(tanggalMasuk),
            isiSurat = isiSurat, kesimpulan = kesimpulan,
            status = "perlu_verifikasi"
        ))
    }
    fun onVerifyAndForward() {
        submitUpdate(UpdateLetterRequest(status = "belum_disposisi"))
    }
    fun onSubmitDisposition() {
        submitUpdate(UpdateLetterRequest(
            disposisi = disposisi, bidangTujuan = bidangTujuan,
            tanggalDisposisi = toUtcTimestamp(tanggalDisposisi),
            status = "sudah_disposisi"
        ))
    }

    fun onAjukanPersetujuan() {
        submitUpdate(UpdateLetterRequest(
            judulSurat = judulSurat, pengirim = pengirim, nomorSurat = nomorSurat,
            nomorAgenda = nomorAgenda, prioritas = prioritas,
            tanggalSurat = toUtcTimestamp(tanggalSurat),
            isiSurat = isiSurat, kesimpulan = kesimpulan,
            status = "perlu_persetujuan"
        ))
    }

    fun onApproveLetter() {
        submitUpdate(UpdateLetterRequest(
            disposisi = disposisi,
            tanggalDisposisi = toUtcTimestamp(tanggalDisposisi),
            status = "disetujui"
        ))
    }

    fun onRejectRevision() {
        submitUpdate(UpdateLetterRequest(
            disposisi = disposisi, // Director adds revision notes here
            tanggalDisposisi = toUtcTimestamp(tanggalDisposisi),
            status = "perlu_revisi"
        ))
    }

    fun onFinalizeSend() {
        submitUpdate(UpdateLetterRequest(status = "terkirim"))
    }

    private fun submitUpdate(request: UpdateLetterRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = api.updateLetter(letterId, request)
                if (response.status == "success") {
                    _navigateBack.emit(true)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = response.message)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun formatTimestampToDateTime(timestamp: String?): String {
        if (timestamp.isNullOrBlank()) return ""
        return try {
            val utcSdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            utcSdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = utcSdf.parse(timestamp) ?: return ""
            val localSdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            localSdf.timeZone = TimeZone.getDefault()
            localSdf.format(date)
        } catch (e: Exception) {
            try {
                val utcSdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                utcSdf.timeZone = TimeZone.getTimeZone("UTC")
                val date = utcSdf.parse(timestamp) ?: return ""
                val localSdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                localSdf.timeZone = TimeZone.getDefault()
                localSdf.format(date)
            } catch (e2: Exception) { timestamp.take(10) }
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
            try { "${dateTimeString}T00:00:00Z" }
            catch (e2: Exception) { getCurrentUtcTimestamp() }
        }
    }
    private fun getCurrentUtcTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }
}