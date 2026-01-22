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
    var pengirim by mutableStateOf("")
    var judulSurat by mutableStateOf("")
    var tujuan by mutableStateOf("")
    var isiSurat by mutableStateOf("")
    var prioritas by mutableStateOf("biasa")
    var tanggalSurat by mutableStateOf("") // For Masuk
    var tanggalMasuk by mutableStateOf("") // For Masuk

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
                        pengirim = letter.pengirim ?: ""
                        judulSurat = letter.judulSurat ?: ""
                        tujuan = letter.bidangTujuan ?: "" // or 'tujuan' if mapped
                        isiSurat = letter.isiSurat ?: ""
                        prioritas = letter.prioritas ?: "biasa"
                        // Dates need formatting? Backend usually returns ISO string.
                        tanggalSurat = letter.tanggalSurat ?: ""
                        tanggalMasuk = letter.tanggalMasuk ?: ""
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
                                    pengirim =
                                            pengirim, // usually fixed but editable for 'Internal'
                                    // sender text?
                                    judulSurat = judulSurat,
                                    tujuan = tujuan,
                                    isiSurat = isiSurat,
                                    prioritas = prioritas
                                    // File update handled separately usually or ignored for
                                    // metadata edit
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
                                    tanggalSurat = tanggalSurat,
                                    tanggalMasuk = tanggalMasuk
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
