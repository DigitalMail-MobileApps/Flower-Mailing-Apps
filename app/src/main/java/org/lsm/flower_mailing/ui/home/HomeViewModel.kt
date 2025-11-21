package org.lsm.flower_mailing.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.lsm.flower_mailing.data.UserPreferencesRepository
import org.lsm.flower_mailing.data.auth.LogoutRequest
import org.lsm.flower_mailing.data.letter.Letter
import org.lsm.flower_mailing.remote.RetrofitClient

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesRepository = UserPreferencesRepository(application)

    val userRole: StateFlow<String?> = userPreferencesRepository.userRoleFlow
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)
    val userName: StateFlow<String?> = userPreferencesRepository.userNameFlow
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)
    val userEmail: StateFlow<String?> = userPreferencesRepository.userEmailFlow
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)
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
            val token = refreshTokenFlow.first()
            try {
                if (token != null) {
                    RetrofitClient.getInstance(getApplication()).logout(LogoutRequest(token))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                withContext(NonCancellable) {
                    userPreferencesRepository.clearLoginData()
                }
                _isLoggedOut.value = true
            }
        }
    }

    fun fetchInboxLetters() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val role = userRole.first() ?: return@launch
            val statusToFetch = when {
                role.equals("adc", ignoreCase = true) -> "perlu_verifikasi"
                role.equals("direktur", ignoreCase = true) -> "belum_disposisi"
                role.equals("bagian_umum", ignoreCase = true) -> "perlu_verifikasi"
                else -> null
            }

            if (statusToFetch == null) {
                _inboxList.value = emptyList()
                _isLoading.value = false
                return@launch
            }

            try {
                val response = RetrofitClient.getInstance(getApplication()).getLetters(
                    jenisSurat = "masuk",
                    status = statusToFetch
                )
                if (response.status == "success") {
                    _inboxList.value = response.data.items
                        .filter { it.status == statusToFetch }
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

    fun fetchDraftLetters() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val role = userRole.first() ?: return@launch

            val typeToFetch = when {
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
                val response = RetrofitClient.getInstance(getApplication()).getLetters(
                    jenisSurat = typeToFetch,
                    status = "draft"
                )
                if (response.status == "success") {
                    _draftList.value = response.data.items
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
            val role = userRole.first() ?: return@launch

            try {
                val response = RetrofitClient.getInstance(getApplication()).getLetters(
                    jenisSurat = "keluar",
                    status = ""
                )

                if (response.status == "success") {
                    val allLetters = response.data.items

                    val filtered = when {
                        role.equals("adc", ignoreCase = true) -> allLetters.filter {
                            it.jenisSurat == "keluar" && (
                                    it.status == "draft" ||
                                            it.status == "perlu_revisi" ||
                                            it.status == "disetujui"
                                    )
                        }
                        role.equals("direktur", ignoreCase = true) -> allLetters.filter {
                            it.jenisSurat == "keluar" &&
                                    it.status == "perlu_persetujuan"
                        }
                        else -> emptyList()
                    }
                    _suratKeluarList.value = filtered.distinctBy { it.id }
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

    fun fetchHistoryLetters() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = RetrofitClient.getInstance(getApplication()).getLetters(
                    jenisSurat = "masuk",
                    status = "sudah_disposisi"
                )

                val responseKeluar = RetrofitClient.getInstance(getApplication()).getLetters(
                    jenisSurat = "keluar",
                    status = "terkirim"
                )

                val listMasuk = if (response.status == "success") {
                    response.data.items.filter { it.status == "sudah_disposisi" }
                } else emptyList()

                val listKeluar = if (responseKeluar.status == "success") {
                    responseKeluar.data.items.filter { it.status == "terkirim" }
                } else emptyList()

                _historyList.value = (listMasuk + listKeluar)
                    .distinctBy { "${it.jenisSurat}_${it.id}" }
                    .sortedByDescending { it.id }

            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}