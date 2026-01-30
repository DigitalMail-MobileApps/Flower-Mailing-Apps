package org.lsm.flower_mailing.ui.letter

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.lsm.flower_mailing.data.model.request.UpdateIncomingLetterRequest
import org.lsm.flower_mailing.data.model.request.UpdateOutgoingLetterRequest
import org.lsm.flower_mailing.data.repository.IncomingLetterRepository
import org.lsm.flower_mailing.data.repository.OutgoingLetterRepository
import org.lsm.flower_mailing.remote.RetrofitClient

class EditLetterViewModel(application: Application, savedStateHandle: SavedStateHandle) :
        AndroidViewModel(application) {

    private val letterId: String = savedStateHandle.get<String>("letterId") ?: "0"
    private val letterType: String =
            savedStateHandle.get<String>("type") ?: "keluar" // "keluar" or "masuk"

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

    // Form states
    var nomorSurat by mutableStateOf("")
    var nomorAgenda by mutableStateOf("") // For Keluar only
    var pengirim by mutableStateOf("")
    var judulSurat by mutableStateOf("")
    var tujuan by mutableStateOf("")
    var isiSurat by mutableStateOf("")
    var kesimpulan by mutableStateOf("") // For Keluar only
    var prioritas by mutableStateOf("biasa")
    var tanggalSurat by mutableStateOf("") // YYYY-MM-DD
    var tanggalMasuk by mutableStateOf("") // YYYY-MM-DD

    var isLoading by mutableStateOf(true)
    var errorMessage by mutableStateOf<String?>(null)

    private val _navigateBack = MutableSharedFlow<Boolean>()
    val navigateBack = _navigateBack.asSharedFlow()

    init {
        fetchLetterDetails()
    }

    private fun fetchLetterDetails() {
        viewModelScope.launch {
            isLoading = true
            try {
                // Fetch logic similar to Detail but we populate form fields
                // Since generic Get is same for common fields, we can use either repo if
                // getLetterById is exposed or use CommonRepo if we injected it.
                // Assuming we use the specific repo get functions if they exist or Common ones.
                // Wait, I didn't add getLetterById to specific repos.
                // But CommonLetterRepository has it. I should use it.
                val commonRepo =
                        org.lsm.flower_mailing.data.repository.CommonLetterRepository(
                                RetrofitClient.getCommonLetterApi(getApplication())
                        )

                val result = commonRepo.getLetterById(letterId.toIntOrNull() ?: 0)
                if (result.isSuccess) {
                    val letter = result.getOrNull()
                    if (letter != null) {
                        nomorSurat = letter.nomorSurat ?: ""
                        nomorAgenda = letter.nomorAgenda ?: ""
                        pengirim = letter.pengirim ?: ""
                        judulSurat = letter.judulSurat ?: ""
                        tujuan = letter.bidangTujuan ?: ""
                        isiSurat = letter.isiSurat ?: ""
                        kesimpulan = letter.kesimpulan ?: ""
                        prioritas = letter.prioritas ?: "biasa"
                        // Convert ISO timestamp to YYYY-MM-DD for display
                        tanggalSurat = extractDateOnly(letter.tanggalSurat)
                        tanggalMasuk = extractDateOnly(letter.tanggalMasuk)
                    }
                } else {
                    errorMessage = "Gagal memuat data: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /** Extract YYYY-MM-DD from ISO timestamp or return as-is if already in that format */
    private fun extractDateOnly(dateString: String?): String {
        if (dateString.isNullOrBlank()) return ""
        // If it contains 'T', it's probably ISO format - extract date part
        return if (dateString.contains("T")) {
            dateString.substringBefore("T")
        } else {
            // Take first 10 chars if long enough (YYYY-MM-DD = 10 chars)
            if (dateString.length >= 10) dateString.take(10) else dateString
        }
    }

    fun saveChanges() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val id = letterId.toIntOrNull() ?: 0
            if (id == 0) return@launch

            try {
                if (letterType == "keluar") {
                    val request =
                            UpdateOutgoingLetterRequest(
                                    nomorSurat = nomorSurat,
                                    nomorAgenda = nomorAgenda,
                                    pengirim = pengirim,
                                    judulSurat = judulSurat,
                                    tujuan = tujuan,
                                    isiSurat = isiSurat,
                                    kesimpulan = kesimpulan,
                                    prioritas = prioritas,
                                    tanggalSurat = tanggalSurat.ifBlank { null },
                                    tanggalMasuk = tanggalMasuk.ifBlank { null }
                            )
                    val result = outgoingRepo.updateLetter(id, request)
                    if (result.isSuccess) _navigateBack.emit(true)
                    else errorMessage = result.exceptionOrNull()?.message
                } else {
                    val request =
                            UpdateIncomingLetterRequest(
                                    nomorSurat = nomorSurat,
                                    pengirim = pengirim,
                                    judulSurat = judulSurat,
                                    isiSurat = isiSurat,
                                    prioritas = prioritas,
                                    tanggalSurat = tanggalSurat.ifBlank { null },
                                    tanggalMasuk = tanggalMasuk.ifBlank { null }
                            )
                    val result = incomingRepo.updateLetter(id, request)
                    if (result.isSuccess) _navigateBack.emit(true)
                    else errorMessage = result.exceptionOrNull()?.message
                }
            } catch (e: Exception) {
                errorMessage = "Error updating: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
