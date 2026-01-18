package org.lsm.flower_mailing.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.lsm.flower_mailing.data.UserPreferencesRepository
import org.lsm.flower_mailing.data.letter.Letter
import org.lsm.flower_mailing.data.model.response.IncomingLetterDto
import org.lsm.flower_mailing.data.model.response.OutgoingLetterDto
import org.lsm.flower_mailing.data.repository.IncomingLetterRepository
import org.lsm.flower_mailing.data.repository.OutgoingLetterRepository
import org.lsm.flower_mailing.remote.RetrofitClient

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesRepository = UserPreferencesRepository(application)
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

    private val authRepo =
            org.lsm.flower_mailing.data.repository.AuthRepository(
                    RetrofitClient.getAuthApi(application),
                    userPreferencesRepository
            )

    val userRole: StateFlow<String?> =
            userPreferencesRepository.userRoleFlow.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = null
            )
    val userName: StateFlow<String?> =
            userPreferencesRepository.userNameFlow.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = null
            )
    val userEmail: StateFlow<String?> =
            userPreferencesRepository.userEmailFlow.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = null
            )
    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut
    private val refreshTokenFlow = userPreferencesRepository.refreshTokenFlow

    private val _inboxList = MutableStateFlow<List<Letter>>(emptyList())
    val inboxList: StateFlow<List<Letter>> = _inboxList.asStateFlow()

    private val _draftList = MutableStateFlow<List<Letter>>(emptyList())
    val draftList: StateFlow<List<Letter>> = _draftList.asStateFlow()

    private val _historyList = MutableStateFlow<List<Letter>>(emptyList())
    val historyList: StateFlow<List<Letter>> = _historyList.asStateFlow()

    private val _suratKeluarList = MutableStateFlow<List<Letter>>(emptyList())
    val suratKeluarList: StateFlow<List<Letter>> = _suratKeluarList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            try {
                authRepo.logout()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoggedOut.value = true
            }
        }
    }

    fun fetchInboxLetters() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            // Ensure we handle null safely
            val rawRole = userRole.first() ?: return@launch
            val role = rawRole.trim().lowercase()

            try {
                val combinedList = mutableListOf<Letter>()

                // 1. Director Logic
                if (role == "direktur") {
                    // Need Disposition (Masuk)
                    val incomingResult = incomingRepo.getLettersNeedingDisposition()
                    if (incomingResult.isSuccess) {
                        combinedList.addAll(
                                incomingResult.getOrDefault(emptyList()).map { it.toLetter() }
                        )
                    }
                    // Need Approval (Keluar)
                    val outgoingResult = outgoingRepo.getLettersNeedingApproval()
                    if (outgoingResult.isSuccess) {
                        combinedList.addAll(
                                outgoingResult.getOrDefault(emptyList()).map { it.toLetter() }
                        )
                    }
                }
                // 2. Manager Logic
                else if (role.contains("manajer") || role.contains("manager")) {
                    val outgoingResult = outgoingRepo.getLettersNeedingVerification()
                    if (outgoingResult.isSuccess) {
                        combinedList.addAll(
                                outgoingResult.getOrDefault(emptyList()).map { it.toLetter() }
                        )
                    }
                }
                // 3. Fallback / Staff / ADC
                else {
                    // For Staff/ADC, we might revert to old logic or show 'My Letters'
                    // if they are considered "Inbox" (e.g. need revision).
                    // Gap analysis didn't specify "Inbox" for Staff, usually they check status of
                    // their letters.
                    // We'll keep it empty or minimal for now unless specific requirement arises.
                    // Old code: "adc" -> "perlu_verifikasi" (Which implies ADC monitors
                    // verification status?)
                    // If so, we can use getMyLetters() and filter locally.
                }

                _inboxList.value = combinedList.distinctBy { "${it.jenisSurat}_${it.id}" }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchDraftLetters() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val role = userRole.first() ?: return@launch

            val typeToFetch =
                    when {
                        role.equals("bagian_umum", ignoreCase = true) -> "masuk"
                        role.equals("adc", ignoreCase = true) -> "keluar"
                        else -> null
                    }

            if (typeToFetch == null) {
                _draftList.value = emptyList()
                _isLoading.value = false
                return@launch
            }

            try {
                val response =
                        RetrofitClient.getInstance(getApplication())
                                .getLetters(jenisSurat = typeToFetch, status = "draft")
                if (response.status == "success") {
                    _draftList.value =
                            response.data.items
                                    .filter { it.jenisSurat == typeToFetch && it.status == "draft" }
                                    .distinctBy { it.id }
                } else {
                    _errorMessage.value = response.message
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchOutboxLetter() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            // Ensure we handle null safely
            val rawRole = userRole.first() ?: return@launch
            val role = rawRole.trim().lowercase()

            try {
                // 1. ADC Logic (Drafts, Revisions, Approved)
                if (role == "adc") {
                    val result = outgoingRepo.getLetters()
                    if (result.isSuccess) {
                        val filtered =
                                result.getOrDefault(emptyList()).filter {
                                    it.status == "draft" ||
                                            it.status == "perlu_revisi" ||
                                            it.status == "disetujui"
                                }
                        // Map to Letter
                        _suratKeluarList.value = filtered.map { it.toLetter() }.distinctBy { it.id }
                    } else {
                        _errorMessage.value = result.exceptionOrNull()?.message
                    }
                }
                // 2. Director Logic (Need Approval)
                else if (role == "direktur") {
                    val result = outgoingRepo.getLettersNeedingApproval()
                    if (result.isSuccess) {
                        val mapped = result.getOrDefault(emptyList()).map { it.toLetter() }
                        _suratKeluarList.value = mapped
                    } else {
                        _suratKeluarList.value = emptyList()
                    }
                }
                // 3. Manager Logic (Need Verification)
                else if (role.contains("manajer") || role.contains("manager")) {
                    val result = outgoingRepo.getLettersNeedingVerification()
                    if (result.isSuccess) {
                        val mapped = result.getOrDefault(emptyList()).map { it.toLetter() }
                        _suratKeluarList.value = mapped
                    } else {
                        _suratKeluarList.value = emptyList()
                    }
                }
                // 4. Generic Staff (My Letters)
                else {
                    // Try getMyLetters first (per Docs)
                    val result = outgoingRepo.getMyLetters()
                    if (result.isSuccess) {
                        val mapped = result.getOrDefault(emptyList()).map { it.toLetter() }
                    } else {
                        // If 404, maybe fall back to Generic List?
                        // But user specifically said 404 for Generic Endpoint too.
                        // Let's assume getLetters() (GET letters/keluar) is the correct one if
                        // myLetters fails or as primary.
                        // User said "check the api from API_REFERENCE.md" where getMyLetters is
                        // listed.
                        // But reported 404.
                        // Im gonna try getLetters() as the collection resource as a
                        // backup/alternative.
                        val fallback = outgoingRepo.getLetters()
                        if (fallback.isSuccess) {
                            val mapped = fallback.getOrDefault(emptyList()).map { it.toLetter() }
                            _suratKeluarList.value = mapped
                        } else {
                            _errorMessage.value = result.exceptionOrNull()?.message
                        }
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchHistoryLetters() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Use new specific endpoints instead of legacy generic fetch
                val incomingResult = incomingRepo.getLetters()
                val outgoingResult = outgoingRepo.getLetters()

                val listMasuk =
                        if (incomingResult.isSuccess) {
                            incomingResult
                                    .getOrDefault(emptyList())
                                    .filter { it.status == "sudah_disposisi" }
                                    .map { it.toLetter() }
                        } else emptyList()

                val listKeluar =
                        if (outgoingResult.isSuccess) {
                            outgoingResult
                                    .getOrDefault(emptyList())
                                    .filter {
                                        it.status == "terkirim" ||
                                                it.status == "diarsipkan" ||
                                                it.status == "disetujui"
                                    } // Adjust 'terkirim' assumption
                                    .map { it.toLetter() }
                        } else emptyList()

                _historyList.value =
                        (listMasuk + listKeluar)
                                .distinctBy { "${it.jenisSurat}_${it.id}" }
                                .sortedByDescending { it.id } // or createdAt logic
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- Mappers ---

    private fun OutgoingLetterDto.toLetter(): Letter {
        return Letter(
                id = id,
                judulSurat = judulSurat,
                pengirim = "Anda",
                nomorSurat = nomorSurat,
                status = status,
                tanggalSurat = tanggalSurat ?: "",
                tanggalMasuk = "",
                jenisSurat = "keluar",
                prioritas = null,
                isiSurat = null,
                nomorAgenda = "",
                disposisi = null,
                tanggalDisposisi = "",
                bidangTujuan = tujuan,
                kesimpulan = null,
                filePath = filePath,
                createdAt = tanggalSurat ?: ""
        )
    }

    private fun IncomingLetterDto.toLetter(): Letter {
        return Letter(
                id = id,
                judulSurat = judulSurat,
                pengirim = pengirim,
                nomorSurat = nomorSurat,
                status = status,
                tanggalSurat = tanggalSurat ?: "",
                tanggalMasuk = tanggalMasuk,
                jenisSurat = "masuk",
                prioritas = prioritas,
                isiSurat = isiSurat,
                nomorAgenda = "",
                disposisi = null,
                tanggalDisposisi = "",
                bidangTujuan = null, // Incoming doesn't have 'tujuan' field in same way
                kesimpulan = null,
                filePath = fileScanPath,
                createdAt = tanggalMasuk
        )
    }
}
