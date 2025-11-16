package org.lsm.flower_mailing.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val _inboxSuratList = MutableStateFlow<List<Letter>>(emptyList())
    val inboxSuratList: StateFlow<List<Letter>> = _inboxSuratList.asStateFlow()
    private val _draftSuratList = MutableStateFlow<List<Letter>>(emptyList())
    val draftSuratList: StateFlow<List<Letter>> = _draftSuratList.asStateFlow()

    private val _isLoadingSurat = MutableStateFlow(false)
    val isLoadingSurat: StateFlow<Boolean> = _isLoadingSurat.asStateFlow()

    private val _errorMessageSurat = MutableStateFlow<String?>(null)
    val errorMessageSurat: StateFlow<String?> = _errorMessageSurat.asStateFlow()

    val userRole: StateFlow<String?> = userPreferencesRepository.userRoleFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val userName: StateFlow<String?> = userPreferencesRepository.userNameFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val userEmail: StateFlow<String?> = userPreferencesRepository.userEmailFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut

    private val refreshTokenFlow = userPreferencesRepository.refreshTokenFlow

    fun fetchSurat() {
        viewModelScope.launch {
            _isLoadingSurat.value = true
            _errorMessageSurat.value = null
            try {
                val response = RetrofitClient.getInstance(getApplication()).getLetters("masuk")

                if (response.status == "success") {
                    val allLetters = response.data.items
                    _inboxSuratList.value = allLetters.filter { it.status != "draft" }
                    _draftSuratList.value = allLetters.filter { it.status == "draft" }

                } else {
                    _errorMessageSurat.value = response.message
                }
            } catch (e: Exception) {
                _errorMessageSurat.value = e.message ?: "An unknown error occurred"
            } finally {
                _isLoadingSurat.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                val refreshToken = refreshTokenFlow.first()

                if (refreshToken != null) {
                    RetrofitClient.getInstance(getApplication()).logout(LogoutRequest(refreshToken))
                } else {
                    throw IllegalStateException("Refresh token kosong, aksi logout tertahan.")
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

}