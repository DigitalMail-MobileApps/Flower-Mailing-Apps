package org.lsm.flower_mailing.ui.auth

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.lsm.flower_mailing.data.UserPreferencesRepository
import org.lsm.flower_mailing.data.auth.ForgotPasswordRequest
import org.lsm.flower_mailing.data.auth.LoginRequest
import org.lsm.flower_mailing.remote.RetrofitClient

class LoginViewModel(application: Application) : AndroidViewModel(application) {
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


    private val userPreferencesRepository = UserPreferencesRepository(application)

    init {
        viewModelScope.launch {
            val token = userPreferencesRepository.accessTokenFlow.first()
            _isLoggedIn.value = (token != null)
        }
    }
    fun login() {
        viewModelScope.launch {
            isLoading = true

            try {
                val response = RetrofitClient.getInstance(getApplication()).login(LoginRequest(email, password))
                val token = response.data.accessToken
                val refreshToken = response.data.refreshToken
                val role = response.data.user.role
                val user = response.data.user
                val userName = "${user.firstName} ${user.lastName}"
                val userEmail = user.email

                userPreferencesRepository.saveLoginData(
                    token, refreshToken, role, userName, userEmail
                )

                _isLoggedIn.value = true
                loginError = null

            } catch (e: Exception) {
                loginError = e.message ?: "Terjadi error saat prose masuk"
                _isLoggedIn.value = false
            } finally {
                isLoading = false
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.getInstance(getApplication()).forgotPassword(ForgotPasswordRequest(email))
                forgotPasswordMessage = response.message
            } catch (e: Exception) {
                forgotPasswordMessage = e.message ?: "Terjadi error saat mereset password"
            }
        }
    }

    fun onLogout() {
        _isLoggedIn.value = false
    }
}
