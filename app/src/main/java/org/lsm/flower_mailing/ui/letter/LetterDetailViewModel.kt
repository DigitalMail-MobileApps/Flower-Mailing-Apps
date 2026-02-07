package org.lsm.flower_mailing.ui.letter

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.lsm.flower_mailing.data.UserPreferencesRepository
import org.lsm.flower_mailing.data.letter.Letter
import org.lsm.flower_mailing.data.letter.UpdateLetterRequest
import org.lsm.flower_mailing.data.repository.IncomingLetterRepository
import org.lsm.flower_mailing.data.repository.OutgoingLetterRepository
import org.lsm.flower_mailing.remote.RetrofitClient

data class LetterUiState(
        val isLoading: Boolean = true,
        val isLetterInfoEditable: Boolean = false,
        val isDispositionSectionVisible: Boolean = false,
        val isDispositionInfoEditable: Boolean = false,
        val downloadUrl: String? = null,
        val buttons: List<LetterButtonType> = emptyList(),
        val errorMessage: String? = null,
        val status: String = ""
)

enum class LetterButtonType {
        // Flow surat masuk
        SAVE_DRAFT,
        SUBMIT_TO_STAF_PROGRAM,
        VERIFY_AND_FORWARD,
        VERIFY_REJECT, // Manager rejects letter during verification
        SUBMIT_DISPOSITION,
        SUBMIT_LETTER, // New button for submitting Surat Masuk draft

        // Flow surat keluar
        AJUKAN_PERSETUJUAN,
        APPROVE_LETTER,
        REJECT_REVISION,
        FINALIZE_SEND,

        // Common
        EDIT,
        DELETE,
        REPLY
}

class LetterDetailViewModel(
        application: Application,
        private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

        private val userPrefsRepo = UserPreferencesRepository(application)
        private val api =
                RetrofitClient.getInstance(
                        application
                ) // Keep for fetching details if generic endpoint exists
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
        private val commonRepo =
                org.lsm.flower_mailing.data.repository.CommonLetterRepository(
                        RetrofitClient.getCommonLetterApi(application)
                )

        val letterId: String = savedStateHandle.get<String>("letterId") ?: "0"
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
        var scope by mutableStateOf("")
        var jenisSurat by mutableStateOf("")
        var needsReply by mutableStateOf(false)

        val prioritasOptions = listOf("biasa", "segera", "penting")
        val disposisiTargetOptions =
                listOf(
                        "Staf Program",
                        "Staf Lembaga",
                        "Manajer",
                        "Keuangan",
                        "Sekretaris",
                        "Lainnya"
                )

        private val _uiState = MutableStateFlow(LetterUiState())
        val uiState = _uiState.asStateFlow()
        private val _navigateBack = MutableSharedFlow<Boolean>()
        val navigateBack = _navigateBack.asSharedFlow()

        private val _navigateToReply = MutableSharedFlow<Triple<Int, String, String>>()
        val navigateToReply = _navigateToReply.asSharedFlow()

        init {
                fetchLetterDetails()
        }

        fun fetchLetterDetails() {
                viewModelScope.launch {
                        _uiState.value = LetterUiState(isLoading = true)
                        try {
                                val userRole = userPrefsRepo.userRoleFlow.first()
                                val response = api.getLetterById(letterId)
                                if (response.success) {
                                        val letter = response.data
                                        originalLetter = letter
                                        populateFormFields(letter)
                                        calculateUiState(userRole, letter)
                                } else {
                                        _uiState.value =
                                                LetterUiState(
                                                        isLoading = false,
                                                        errorMessage = response.message
                                                )
                                }
                        } catch (e: Exception) {
                                _uiState.value =
                                        LetterUiState(
                                                isLoading = false,
                                                errorMessage = e.message ?: "Unknown error"
                                        )
                        }
                }
        }

        private fun populateFormFields(letter: Letter) {
                judulSurat = letter.judulSurat
                pengirim = letter.pengirim
                nomorSurat = letter.nomorSurat
                nomorAgenda = letter.nomorAgenda ?: ""
                prioritas = letter.prioritas ?: "biasa"
                tanggalSurat = formatTimestampToDateTime(letter.tanggalSurat)
                tanggalMasuk = formatTimestampToDateTime(letter.tanggalMasuk)
                isiSurat = letter.isiSurat ?: ""
                kesimpulan = letter.kesimpulan ?: ""
                disposisi = letter.disposisi ?: ""
                bidangTujuan = letter.bidangTujuan ?: ""
                scope = letter.scope ?: ""
                jenisSurat = letter.jenisSurat

                val status = letter.status
                val isActionNeeded =
                        status == "perlu_verifikasi" ||
                                status == "belum_disposisi" ||
                                status == "perlu_persetujuan"

                tanggalDisposisi =
                        if (isActionNeeded) formatMillisToDateTimeString(System.currentTimeMillis())
                        else formatTimestampToDateTime(letter.tanggalDisposisi)

                needsReply = letter.needsReply
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
                val newState =
                        when {
                                role.equals("staf_program", ignoreCase = true) ||
                                        role.equals("staf_lembaga", ignoreCase = true) ->
                                        when (status) {
                                                "draft" ->
                                                        LetterUiState(
                                                                isLoading = false,
                                                                isLetterInfoEditable = true,
                                                                downloadUrl = letter.filePath,
                                                                buttons =
                                                                        listOf(
                                                                                LetterButtonType
                                                                                        .SAVE_DRAFT,
                                                                                LetterButtonType
                                                                                        .AJUKAN_PERSETUJUAN
                                                                        )
                                                        )
                                                "perlu_revisi" ->
                                                        LetterUiState(
                                                                isLoading = false,
                                                                isLetterInfoEditable = true,
                                                                isDispositionSectionVisible =
                                                                        true, // Show notes/reasons
                                                                isDispositionInfoEditable =
                                                                        false, // Read-only notes
                                                                downloadUrl = letter.filePath,
                                                                buttons =
                                                                        listOf(
                                                                                LetterButtonType
                                                                                        .SAVE_DRAFT,
                                                                                LetterButtonType
                                                                                        .AJUKAN_PERSETUJUAN
                                                                        )
                                                        )
                                                "disetujui" ->
                                                        LetterUiState(
                                                                isLoading = false,
                                                                isLetterInfoEditable = false,
                                                                isDispositionSectionVisible = false,
                                                                isDispositionInfoEditable = false,
                                                                downloadUrl = letter.filePath,
                                                                buttons =
                                                                        listOf(
                                                                                LetterButtonType
                                                                                        .FINALIZE_SEND
                                                                        )
                                                        )
                                                else ->
                                                        LetterUiState(
                                                                isLoading = false,
                                                                isLetterInfoEditable = false,
                                                                downloadUrl = letter.filePath,
                                                                buttons = emptyList()
                                                        )
                                        }
                                role.equals("direktur", ignoreCase = true) ->
                                        when (status) {
                                                "perlu_persetujuan" ->
                                                        LetterUiState(
                                                                isLoading = false,
                                                                isLetterInfoEditable = false,
                                                                isDispositionSectionVisible = true,
                                                                isDispositionInfoEditable = true,
                                                                downloadUrl = letter.filePath,
                                                                buttons =
                                                                        listOf(
                                                                                LetterButtonType
                                                                                        .APPROVE_LETTER
                                                                        )
                                                        )
                                                else ->
                                                        LetterUiState(
                                                                isLoading = false,
                                                                isLetterInfoEditable = false,
                                                                isDispositionSectionVisible = true,
                                                                isDispositionInfoEditable = false,
                                                                downloadUrl = letter.filePath,
                                                                buttons = emptyList()
                                                        )
                                        }
                                role?.contains("manajer", ignoreCase = true) == true ->
                                        when (status) {
                                                "perlu_verifikasi" ->
                                                        LetterUiState(
                                                                isLoading = false,
                                                                isLetterInfoEditable = false,
                                                                downloadUrl = letter.filePath,
                                                                buttons =
                                                                        listOf(
                                                                                LetterButtonType
                                                                                        .VERIFY_AND_FORWARD,
                                                                                LetterButtonType
                                                                                        .VERIFY_REJECT
                                                                        )
                                                        )
                                                else ->
                                                        LetterUiState(
                                                                isLoading = false,
                                                                isLetterInfoEditable = false,
                                                                downloadUrl = letter.filePath,
                                                                buttons = emptyList()
                                                        )
                                        }
                                else -> LetterUiState(isLoading = false)
                        }
                _uiState.value = newState.copy(status = status)
        }

        private fun calculateUiStateSuratMasuk(role: String?, status: String, letter: Letter) {
                val newState =
                        when {
                                role.equals("staf_lembaga", ignoreCase = true) ->
                                        when (status) {
                                                "draft" ->
                                                        LetterUiState(
                                                                isLoading = false,
                                                                isLetterInfoEditable = true,
                                                                downloadUrl = letter.filePath,
                                                                buttons =
                                                                        listOf(
                                                                                LetterButtonType
                                                                                        .EDIT,
                                                                                LetterButtonType
                                                                                        .SUBMIT_LETTER, // Submit to
                                                                                // Direktur
                                                                                LetterButtonType
                                                                                        .DELETE
                                                                        )
                                                        )
                                                else ->
                                                        LetterUiState(
                                                                isLoading = false,
                                                                isLetterInfoEditable = false,
                                                                isDispositionSectionVisible = true,
                                                                isDispositionInfoEditable = false,
                                                                downloadUrl = letter.filePath,
                                                                buttons =
                                                                        if (letter.needsReply)
                                                                                listOf(
                                                                                        LetterButtonType
                                                                                                .REPLY
                                                                                )
                                                                        else emptyList()
                                                        )
                                        }
                                role.equals("staf_program", ignoreCase = true) ->
                                        when (status) {
                                                "perlu_verifikasi" ->
                                                        LetterUiState(
                                                                isLoading = false,
                                                                isLetterInfoEditable = false,
                                                                downloadUrl = letter.filePath,
                                                                buttons =
                                                                        emptyList() // Only Manajer
                                                                // can verify
                                                                )
                                                else ->
                                                        LetterUiState(
                                                                isLoading = false,
                                                                isLetterInfoEditable = false,
                                                                isDispositionSectionVisible = true,
                                                                isDispositionInfoEditable = false,
                                                                downloadUrl = letter.filePath,
                                                                buttons =
                                                                        if (letter.needsReply)
                                                                                listOf(
                                                                                        LetterButtonType
                                                                                                .REPLY
                                                                                )
                                                                        else emptyList()
                                                        )
                                        }
                                role.equals("direktur", ignoreCase = true) ->
                                        when (status) {
                                                "belum_disposisi" ->
                                                        LetterUiState(
                                                                isLoading = false,
                                                                isLetterInfoEditable = false,
                                                                isDispositionSectionVisible = true,
                                                                isDispositionInfoEditable = true,
                                                                downloadUrl = letter.filePath,
                                                                buttons =
                                                                        listOf(
                                                                                LetterButtonType
                                                                                        .SUBMIT_DISPOSITION
                                                                        )
                                                        )
                                                else ->
                                                        LetterUiState(
                                                                isLoading = false,
                                                                isLetterInfoEditable = false,
                                                                isDispositionSectionVisible = true,
                                                                isDispositionInfoEditable = false,
                                                                downloadUrl = letter.filePath,
                                                                buttons = emptyList()
                                                        )
                                        }
                                // Manajer roles can view surat masuk (read-only, for information)
                                role?.contains("manajer", ignoreCase = true) == true ->
                                        LetterUiState(
                                                isLoading = false,
                                                isLetterInfoEditable = false,
                                                isDispositionSectionVisible = true,
                                                isDispositionInfoEditable = false,
                                                downloadUrl = letter.filePath,
                                                buttons = emptyList()
                                        )
                                // Fallback for other roles (e.g., admin) - read-only view without error
                                else ->
                                        LetterUiState(
                                                isLoading = false,
                                                isLetterInfoEditable = false,
                                                downloadUrl = letter.filePath,
                                                buttons = emptyList()
                                        )
                        }
                _uiState.value = newState.copy(status = status)
        }

        fun onSaveDraft() {
                // Save draft for Surat Keluar
                // Must use OutgoingLetterRepository for correct endpoint: PUT letters/keluar/{id}
                viewModelScope.launch {
                        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                        try {
                                val request = org.lsm.flower_mailing.data.model.request.UpdateOutgoingLetterRequest(
                                        judulSurat = judulSurat,
                                        pengirim = pengirim,
                                        nomorSurat = nomorSurat,
                                        nomorAgenda = nomorAgenda,
                                        prioritas = prioritas,
                                        tanggalSurat = toUtcTimestamp(tanggalSurat),
                                        isiSurat = isiSurat,
                                        kesimpulan = kesimpulan,
                                        status = "draft"
                                )
                                val result = outgoingRepo.updateLetter(letterId.toInt(), request)
                                if (result.isSuccess) {
                                        _navigateBack.emit(true)
                                } else {
                                        _uiState.value = _uiState.value.copy(
                                                isLoading = false,
                                                errorMessage = result.exceptionOrNull()?.message
                                        )
                                }
                        } catch (e: Exception) {
                                _uiState.value = _uiState.value.copy(
                                        isLoading = false,
                                        errorMessage = e.message
                                )
                        }
                }
        }

        fun onSubmitLetter() {
                // Use incomingRepo specifically to hit /api/letters/masuk/:id
                viewModelScope.launch {
                        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                        try {
                                val request =
                                        org.lsm.flower_mailing.data.model.request
                                                .UpdateIncomingLetterRequest(
                                                        judulSurat = judulSurat,
                                                        pengirim = pengirim,
                                                        nomorSurat = nomorSurat,
                                                        // nomorAgenda not in UpdateIncoming? Check
                                                        // DTO.
                                                        // UpdateIncomingLetterRequest has:
                                                        // nomorSurat, pengirim, judulSurat,
                                                        // tanggalSurat, tanggalMasuk, scope,
                                                        // prioritas, isiSurat, status
                                                        // It does NOT have nomorAgenda (based on my
                                                        // view_file earlier of
                                                        // UpdateIncomingLetterRequest.kt)
                                                        // Let's verify DTO fields.
                                                        prioritas = prioritas,
                                                        tanggalSurat = toUtcTimestamp(tanggalSurat),
                                                        isiSurat = isiSurat,
                                                        kesimpulan = kesimpulan,
                                                        status = "belum_disposisi"
                                                )
                                val result = incomingRepo.updateLetter(letterId.toInt(), request)
                                if (result.isSuccess) {
                                        _navigateBack.emit(true)
                                } else {
                                        _uiState.value =
                                                _uiState.value.copy(
                                                        isLoading = false,
                                                        errorMessage =
                                                                result.exceptionOrNull()?.message
                                                )
                                }
                        } catch (e: Exception) {
                                _uiState.value =
                                        _uiState.value.copy(
                                                isLoading = false,
                                                errorMessage = e.message
                                        )
                        }
                }
        }

        fun onSubmitToStafProgram() { // Renamed from onSubmitToAdc
                submitUpdate(
                        UpdateLetterRequest(
                                judulSurat = judulSurat,
                                pengirim = pengirim,
                                nomorSurat = nomorSurat,
                                nomorAgenda = nomorAgenda,
                                prioritas = prioritas,
                                tanggalSurat = toUtcTimestamp(tanggalSurat),
                                tanggalMasuk = toUtcTimestamp(tanggalMasuk),
                                isiSurat = isiSurat,
                                kesimpulan = kesimpulan,
                                status = "perlu_verifikasi"
                        )
                )
        }
        fun onVerifyAndForward() {
                // Manager KPP verifies the letter (Surat Keluar)
                viewModelScope.launch {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                        try {
                                val result = outgoingRepo.verifyLetter(letterId.toInt())
                                if (result.isSuccess) {
                                        _navigateBack.emit(true)
                                } else {
                                        _uiState.value =
                                                _uiState.value.copy(
                                                        isLoading = false,
                                                        errorMessage =
                                                                result.exceptionOrNull()?.message
                                                )
                                }
                        } catch (e: Exception) {
                                _uiState.value =
                                        _uiState.value.copy(
                                                isLoading = false,
                                                errorMessage = e.message
                                        )
                        }
                }
        }

        fun onVerifyReject() {
                // Manager rejects the letter (Surat Keluar) during verification
                viewModelScope.launch {
                        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                        try {
                                val result = outgoingRepo.verifyLetterReject(letterId.toInt())
                                if (result.isSuccess) {
                                        _navigateBack.emit(true)
                                } else {
                                        _uiState.value = _uiState.value.copy(
                                                isLoading = false,
                                                errorMessage = result.exceptionOrNull()?.message
                                        )
                                }
                        } catch (e: Exception) {
                                _uiState.value = _uiState.value.copy(
                                        isLoading = false,
                                        errorMessage = e.message
                                )
                        }
                }
        }

        fun onSubmitDisposition() {
                // Director adds disposition (Surat Masuk)
                viewModelScope.launch {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                        try {
                                // TODO: Add UI for selecting target users.
                                // val targetUserIds = emptyList<Int>() // REMOVED: Backend uses
                                // string target
                                val result =
                                        incomingRepo.disposeLetter(
                                                letterId.toInt(),
                                                disposisi,
                                                bidangTujuan,
                                                needsReply
                                        )
                                if (result.isSuccess) {
                                        _navigateBack.emit(true)
                                } else {
                                        _uiState.value =
                                                _uiState.value.copy(
                                                        isLoading = false,
                                                        errorMessage =
                                                                result.exceptionOrNull()?.message
                                                )
                                }
                        } catch (e: Exception) {
                                _uiState.value =
                                        _uiState.value.copy(
                                                isLoading = false,
                                                errorMessage = e.message
                                        )
                        }
                }
        }

        fun onAjukanPersetujuan() {
                // Staff submits draft for verification by Manager
                // Status transition: Draft -> Perlu Verifikasi
                // Must use OutgoingLetterRepository to hit the correct endpoint: PUT letters/keluar/{id}
                // The legacy submitUpdate() uses PUT letters/{id} which doesn't exist (405 error)
                viewModelScope.launch {
                        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                        try {
                                val request = org.lsm.flower_mailing.data.model.request.UpdateOutgoingLetterRequest(
                                        judulSurat = judulSurat,
                                        pengirim = pengirim,
                                        nomorSurat = nomorSurat,
                                        nomorAgenda = nomorAgenda,
                                        prioritas = prioritas,
                                        tanggalSurat = toUtcTimestamp(tanggalSurat),
                                        isiSurat = isiSurat,
                                        kesimpulan = kesimpulan,
                                        status = "perlu_verifikasi" // Draft -> Verifikasi (Manajer)
                                )
                                val result = outgoingRepo.updateLetter(letterId.toInt(), request)
                                if (result.isSuccess) {
                                        _navigateBack.emit(true)
                                } else {
                                        _uiState.value = _uiState.value.copy(
                                                isLoading = false,
                                                errorMessage = result.exceptionOrNull()?.message
                                        )
                                }
                        } catch (e: Exception) {
                                _uiState.value = _uiState.value.copy(
                                        isLoading = false,
                                        errorMessage = e.message
                                )
                        }
                }
        }

        fun onApproveLetter() {
                // Director approves (Surat Keluar)
                viewModelScope.launch {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                        try {
                                val result = outgoingRepo.approveLetter(letterId.toInt())
                                if (result.isSuccess) {
                                        _navigateBack.emit(true)
                                } else {
                                        val errorMsg =
                                                result.exceptionOrNull()?.message ?: "Unknown error"
                                        val userFriendlyMsg =
                                                when {
                                                        errorMsg.contains("500") ||
                                                                errorMsg.contains(
                                                                        "Internal Server"
                                                                ) ||
                                                                errorMsg.contains(
                                                                        "Gagal memproses"
                                                                ) ->
                                                                "Server sedang bermasalah. Coba lagi nanti atau hubungi admin."
                                                        errorMsg.contains("403") ||
                                                                errorMsg.contains("Forbidden") ->
                                                                "Anda tidak memiliki izin untuk menyetujui surat ini."
                                                        errorMsg.contains("404") ||
                                                                errorMsg.contains("Not found") ->
                                                                "Surat tidak ditemukan."
                                                        else -> errorMsg
                                                }
                                        _uiState.value =
                                                _uiState.value.copy(
                                                        isLoading = false,
                                                        errorMessage = userFriendlyMsg
                                                )
                                }
                        } catch (e: Exception) {
                                val userFriendlyMsg =
                                        when {
                                                e.message?.contains("500") == true ->
                                                        "Server sedang bermasalah. Coba lagi nanti atau hubungi admin."
                                                else -> e.message
                                                                ?: "Terjadi kesalahan tidak diketahui"
                                        }
                                _uiState.value =
                                        _uiState.value.copy(
                                                isLoading = false,
                                                errorMessage = userFriendlyMsg
                                        )
                        }
                }
        }

        fun onRejectRevision() {
                // Director rejects Surat Keluar - status to 'perlu_revisi'
                // Must use OutgoingLetterRepository for correct endpoint: PUT letters/keluar/{id}
                viewModelScope.launch {
                        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                        try {
                                val request = org.lsm.flower_mailing.data.model.request.UpdateOutgoingLetterRequest(
                                        status = "perlu_revisi"
                                )
                                val result = outgoingRepo.updateLetter(letterId.toInt(), request)
                                if (result.isSuccess) {
                                        _navigateBack.emit(true)
                                } else {
                                        _uiState.value = _uiState.value.copy(
                                                isLoading = false,
                                                errorMessage = result.exceptionOrNull()?.message
                                        )
                                }
                        } catch (e: Exception) {
                                _uiState.value = _uiState.value.copy(
                                        isLoading = false,
                                        errorMessage = e.message
                                )
                        }
                }
        }

        fun onFinalizeSend() {
                viewModelScope.launch {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                        try {
                                val result = outgoingRepo.archiveLetter(letterId.toInt())
                                if (result.isSuccess) {
                                        _navigateBack.emit(true)
                                } else {
                                        _uiState.value =
                                                _uiState.value.copy(
                                                        isLoading = false,
                                                        errorMessage =
                                                                result.exceptionOrNull()?.message
                                                )
                                }
                        } catch (e: Exception) {
                                _uiState.value =
                                        _uiState.value.copy(
                                                isLoading = false,
                                                errorMessage = e.message
                                        )
                        }
                }
        }



        fun deleteLetter() {
                viewModelScope.launch {
                        try {
                                _uiState.value =
                                        _uiState.value.copy(isLoading = true, errorMessage = null)
                                val id = letterId.toIntOrNull() ?: 0
                                if (id == 0) return@launch

                                val result = commonRepo.deleteLetter(id)
                                if (result.isSuccess) {
                                        // Navigate back on success
                                        _navigateBack.emit(true)
                                } else {
                                        _uiState.value =
                                                _uiState.value.copy(
                                                        isLoading = false,
                                                        errorMessage =
                                                                "Gagal menghapus surat: ${result.exceptionOrNull()?.message}"
                                                )
                                }
                        } catch (e: Exception) {
                                _uiState.value =
                                        _uiState.value.copy(
                                                isLoading = false,
                                                errorMessage = "Error: ${e.message}"
                                        )
                        }
                }
        }

        private fun submitUpdate(request: UpdateLetterRequest) {
                viewModelScope.launch {
                        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                        try {
                                val response = api.updateLetter(letterId, request)
                                if (response.success) {
                                        _navigateBack.emit(true)
                                } else {
                                        _uiState.value =
                                                _uiState.value.copy(
                                                        isLoading = false,
                                                        errorMessage = response.message
                                                )
                                }
                        } catch (e: Exception) {
                                _uiState.value =
                                        _uiState.value.copy(
                                                isLoading = false,
                                                errorMessage = e.message
                                        )
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
                        } catch (e2: Exception) {
                                timestamp.take(10)
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
                        val date =
                                localSdf.parse(dateTimeString)
                                        ?: throw IllegalArgumentException("Invalid date")
                        val utcSdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                        utcSdf.timeZone = TimeZone.getTimeZone("UTC")
                        utcSdf.format(date)
                } catch (e: Exception) {
                        try {
                                "${dateTimeString}T00:00:00Z"
                        } catch (e2: Exception) {
                                getCurrentUtcTimestamp()
                        }
                }
        }
        private fun getCurrentUtcTimestamp(): String {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                return sdf.format(Date())
        }

        fun onReply() {
                viewModelScope.launch {
                        _navigateToReply.emit(Triple(letterId.toInt(), judulSurat, pengirim))
                }
        }
}
