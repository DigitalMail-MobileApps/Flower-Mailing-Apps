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

    // Lists
    private val _inboxList = MutableStateFlow<List<Letter>>(emptyList())
    val inboxList: StateFlow<List<Letter>> = _inboxList.asStateFlow()

    private val _draftList = MutableStateFlow<List<Letter>>(emptyList())
    val draftList: StateFlow<List<Letter>> = _draftList.asStateFlow()

    private val _historyList = MutableStateFlow<List<Letter>>(emptyList())
    val historyList: StateFlow<List<Letter>> = _historyList.asStateFlow()

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
                    _inboxList.value = response.data.items.filter { it.status == statusToFetch }
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

            val role = userRole.first()
            if (role != "bagian_umum") {
                _draftList.value = emptyList()
                _isLoading.value = false
                return@launch
            }

            try {
                val response = RetrofitClient.getInstance(getApplication()).getLetters(
                    jenisSurat = "masuk",
                    status = "draft"
                )
                if (response.status == "success") {
                    _draftList.value = response.data.items.filter { it.status == "draft" }
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
                if (response.status == "success") {
                    _historyList.value = response.data.items.filter { it.status == "sudah_disposisi" }
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
}