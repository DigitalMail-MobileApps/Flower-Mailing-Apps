package org.lsm.flower_mailing.ui.settings

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.lsm.flower_mailing.data.UserPreferencesRepository
import org.lsm.flower_mailing.data.auth.User
import org.lsm.flower_mailing.data.model.request.ChangePasswordRequest
import org.lsm.flower_mailing.data.repository.AuthRepository
import org.lsm.flower_mailing.data.repository.SettingsRepository
import org.lsm.flower_mailing.remote.RetrofitClient

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    // Manual Injection
    private val preferences = UserPreferencesRepository(application)
    private val settingsRepo =
            SettingsRepository(RetrofitClient.getSettingsApi(application), preferences)
    private val authRepo = AuthRepository(RetrofitClient.getAuthApi(application), preferences)

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile = _userProfile.asStateFlow()

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    // Edit Fields
    var editFirstName by mutableStateOf("")
    var editLastName by mutableStateOf("")

    // Read-only or potentially missing fields depending on DTO
    var editJabatan by mutableStateOf("")
    var editAtribut by mutableStateOf("")

    // Password Fields
    var oldPassword by mutableStateOf("")
    var newPassword by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        viewModelScope.launch {
            isLoading = true
            val result = settingsRepo.getProfile()
            if (result.isSuccess) {
                val user = result.getOrNull()
                _userProfile.value = user
                user?.let {
                    editFirstName = it.firstName ?: ""
                    editLastName = it.lastName ?: ""
                    editJabatan = it.jabatan ?: ""
                    // editAtribut = it.atribut // Atribut not in default User DTO
                }
            } else {
                errorMessage = result.exceptionOrNull()?.message
            }
            isLoading = false
        }
    }

    fun updateProfile(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            val result = settingsRepo.updateProfile(editFirstName, editLastName)

            if (result.isSuccess) {
                _userProfile.value = result.getOrNull()
                successMessage = "Profil berhasil diperbarui"
                onSuccess()
            } else {
                errorMessage = result.exceptionOrNull()?.message
            }
            isLoading = false
        }
    }

    fun changePassword(onSuccess: () -> Unit) {
        if (newPassword != confirmPassword) {
            errorMessage = "Konfirmasi password tidak cocok"
            return
        }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            val request =
                    ChangePasswordRequest(
                            oldPassword = oldPassword,
                            newPassword = newPassword,
                            confirmPassword = confirmPassword
                    )

            val result = settingsRepo.changePassword(request)

            if (result.isSuccess) {
                successMessage = "Password berhasil diubah"
                oldPassword = ""
                newPassword = ""
                confirmPassword = ""
                onSuccess()
            } else {
                errorMessage = result.exceptionOrNull()?.message
            }
            isLoading = false
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            authRepo.logout() // Repository handles token clearing and API call (if implemented)
            isLoading = false
            onLogoutComplete()
        }
    }
}
