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

    init {
        viewModelScope.launch {
            userRole.collect { role ->
                if (!role.isNullOrBlank()) {
                    fetchAllData()
                }
            }
        }
        // Also ensure data is refreshed when creating viewmodel if role is already there
        // (Handled by collect above)
    }

    fun fetchAllData() {
        fetchInboxLetters()
        fetchOutboxLetter()
        fetchDraftLetters()
        fetchHistoryLetters()
    }

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

                // 1. Director Logic - Only Surat Masuk (Need Disposition)
                if (role == "direktur") {
                    val incomingResult = incomingRepo.getLettersNeedingDisposition()
                    if (incomingResult.isSuccess) {
                        combinedList.addAll(
                                incomingResult.getOrDefault(emptyList()).map { it.toLetter() }
                        )
                    }
                    // NOTE: Surat Keluar (Need Approval) is fetched separately in
                    // fetchOutboxLetter()
                }
                // 2. Manager Logic - Don't populate inboxList, they use suratKeluarList only
                else if (role.contains("manajer") || role.contains("manager")) {
                    // Verification letters are fetched in fetchOutboxLetter()
                    // Manager doesn't have access to Surat Masuk tab
                }
                // 3. Staf Lembaga - Fetches their registered Surat Masuk AND Needs Reply
                else if (role == "staf_lembaga") {
                    // Fetch what they registered
                    val myLettersResult = incomingRepo.getMyLetters()
                    if (myLettersResult.isSuccess) {
                        combinedList.addAll(
                                myLettersResult
                                        .getOrDefault(emptyList())
                                        .filter { it.status != "diarsipkan" }
                                        .map { it.toLetter() }
                        )
                    }

                    // NEW: Fetch what needs reply (Internal scope handled by backend)
                    val needsReplyResult = incomingRepo.getLettersNeedingReply()
                    if (needsReplyResult.isSuccess) {
                        combinedList.addAll(
                                needsReplyResult.getOrDefault(emptyList()).map { it.toLetter() }
                        )
                    }
                }
                // 4. Staf Program - Fetches Needs Reply (Eksternal/All)
                else if (role == "staf_program") {
                    val needsReplyResult = incomingRepo.getLettersNeedingReply()
                    if (needsReplyResult.isSuccess) {
                        combinedList.addAll(
                                needsReplyResult.getOrDefault(emptyList()).map { it.toLetter() }
                        )
                    }
                }
                // 5. Generic/Other
                else {
                    // No inbox for others
                }

                // Distinct by ID to avoid duplicates if a letter appears in both lists (unlikely
                // but safe)
                _inboxList.value =
                        combinedList.distinctBy { "${it.jenisSurat}_${it.id}" }.sortedByDescending {
                            it.id
                        } // Sort by newest first
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
                        role.equals("staf_lembaga", ignoreCase = true) -> "masuk"
                        role.equals("staf_program", ignoreCase = true) -> "keluar"
                        else -> null
                    }

            if (typeToFetch == null) {
                _draftList.value = emptyList()
                _isLoading.value = false
                return@launch
            }

            try {
                // New Logic using Repositories
                if (typeToFetch == "keluar") {
                    val result = outgoingRepo.getMyLetters()
                    if (result.isSuccess) {
                        val allLetters = result.getOrDefault(emptyList())
                        _draftList.value =
                                allLetters
                                        .filter { it.status == "draft" }
                                        .map { it.toLetter() }
                                        .distinctBy { it.id }
                    } else {
                        _errorMessage.value = result.exceptionOrNull()?.message
                    }
                } else if (typeToFetch == "masuk") {
                    // For incoming letters, "draft" concept might be "belum_disposisi" or strictly
                    // unused.
                    // Assuming for "bagian_umum" (General Affairs), "draft" might mean unregistered
                    // fully?
                    // Or maybe they just want to see what they just registered.
                    // For now, let's look at "belum_disposisi" as a 'pending' state for them?
                    // Or if there is a true "draft" status for incoming.
                    // Based on previous code: status="draft".
                    // Let's assume we filter local 'masuk' letters for 'draft' if that status
                    // exists.
                    val result = incomingRepo.getMyLetters()
                    if (result.isSuccess) {
                        val allLetters = result.getOrDefault(emptyList())
                        _draftList.value =
                                allLetters
                                        .filter { it.status == "draft" } // Pending clarification
                                        .map { it.toLetter() }
                                        .distinctBy { it.id }
                    } else {
                        _errorMessage.value = result.exceptionOrNull()?.message
                    }
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
                // 1. Staf Program Logic (All their letters: Drafts, Pending, Revisions, Approved)
                if (role == "staf_program") {
                    val result = outgoingRepo.getMyLetters()
                    if (result.isSuccess) {
                        val filtered =
                                result.getOrDefault(emptyList()).filter {
                                    it.status == "draft" ||
                                            it.status == "perlu_verifikasi" ||
                                            it.status == "perlu_persetujuan" ||
                                            it.status == "perlu_revisi" ||
                                            it.status == "disetujui"
                                    // Removed "diarsipkan" as requested
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
                // 4. Staf Lembaga Logic (Internal Surat Keluar - same as staf_program)
                else if (role == "staf_lembaga") {
                    val result = outgoingRepo.getMyLetters()
                    if (result.isSuccess) {
                        val filtered =
                                result.getOrDefault(emptyList()).filter {
                                    it.status == "draft" ||
                                            it.status == "perlu_verifikasi" ||
                                            it.status == "perlu_persetujuan" ||
                                            it.status == "perlu_revisi" ||
                                            it.status == "disetujui"
                                    // Removed "diarsipkan" as requested
                                }
                        _suratKeluarList.value = filtered.map { it.toLetter() }.distinctBy { it.id }
                    } else {
                        _errorMessage.value = result.exceptionOrNull()?.message
                    }
                }
                // 5. Generic Staff / Admin (No outbox permissions typically)
                else {
                    // Admin and unknown roles - empty or no-op
                    _suratKeluarList.value = emptyList()
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

            val rawRole = userRole.first() ?: return@launch
            val role = rawRole.trim().lowercase()

            try {
                // Direktur and Managers don't have "my letters" - they approve/verify
                // They don't have a history endpoint, so show empty or archived letters they
                // processed
                if (role.contains("manajer") || role.contains("manager") || role == "admin") {
                    // For now, show empty history for these roles
                    // In the future, could add an endpoint for "letters I've approved/verified"
                    _historyList.value = emptyList()
                } else if (role == "direktur") {
                    val result = incomingRepo.getMyDispositions()
                    if (result.isSuccess) {
                        _historyList.value =
                                result.getOrDefault(emptyList())
                                        .map { it.toLetter() }
                                        .sortedByDescending { it.id }
                    }
                } else {
                    // Staff roles (staf_program, staf_lembaga) - fetch their created letters
                    val combinedList = mutableListOf<Letter>()

                    // Only staf_lembaga fetches incoming letters
                    if (role == "staf_lembaga") {
                        val incomingResult = incomingRepo.getMyLetters()
                        if (incomingResult.isSuccess) {
                            combinedList.addAll(
                                    incomingResult
                                            .getOrDefault(emptyList())
                                            .filter {
                                                it.status ==
                                                        "diarsipkan" // Only show 'diarsipkan' in
                                                // History
                                            }
                                            .map { it.toLetter() }
                            )
                        }
                    }

                    // Both staff types fetch outgoing letters
                    val outgoingResult = outgoingRepo.getMyLetters()
                    if (outgoingResult.isSuccess) {
                        combinedList.addAll(
                                outgoingResult
                                        .getOrDefault(emptyList())
                                        .filter {
                                            it.status == "diarsipkan" || it.status == "disetujui"
                                        }
                                        .map { it.toLetter() }
                        )
                    }

                    _historyList.value =
                            combinedList
                                    .distinctBy { "${it.jenisSurat}_${it.id}" }
                                    .sortedByDescending { it.id }
                }
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
                tanggalMasuk = tanggalMasuk ?: "",
                jenisSurat = "masuk",
                prioritas = prioritas,
                isiSurat = isiSurat,
                nomorAgenda = "",
                disposisi = null,
                tanggalDisposisi = "",
                bidangTujuan = null, // Incoming doesn't have 'tujuan' field in same way
                kesimpulan = null,
                filePath = fileScanPath,
                createdAt = tanggalMasuk ?: createdAt ?: ""
        )
    }
}
