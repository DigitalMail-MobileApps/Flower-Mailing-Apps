package org.lsm.flower_mailing.ui.auth

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.lsm.flower_mailing.data.UserPreferencesRepository
import org.lsm.flower_mailing.data.auth.LoginRequest
import org.lsm.flower_mailing.data.repository.AuthRepository
import org.lsm.flower_mailing.remote.RetrofitClient

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    // Ideally injected via Hilt/Koin, but manual injection for now
    private val authRepository =
            AuthRepository(
                    authApi = RetrofitClient.getAuthApi(application),
                    preferences = UserPreferencesRepository(application)
            )

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var loginError by mutableStateOf<String?>(null)
        private set

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn

    var forgotPasswordMessage by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            val token = authRepository.isLoggedIn.first()
            _isLoggedIn.value = (token != null)
        }
    }

    fun login() {
        if (email.isBlank() || password.isBlank()) {
            loginError = "Email dan password harus diisi"
            return
        }

        viewModelScope.launch {
            isLoading = true
            loginError = null

            val result = authRepository.login(LoginRequest(email, password))

            result.onSuccess { _isLoggedIn.value = true }.onFailure { e ->
                loginError = e.message ?: "Terjadi kesalahan saat login"
                _isLoggedIn.value = false
            }

            isLoading = false
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            val result = authRepository.forgotPassword(email)
            result.onSuccess { msg -> forgotPasswordMessage = msg }.onFailure { e ->
                forgotPasswordMessage = e.message ?: "Gagal mereset password"
            }
        }
    }

    fun onLogout() {
        viewModelScope.launch {
            authRepository.logout()
            _isLoggedIn.value = false
        }
    }
}
