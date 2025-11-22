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
import org.lsm.flower_mailing.data.auth.LogoutRequest
import org.lsm.flower_mailing.data.settings.ChangePasswordRequest
import org.lsm.flower_mailing.data.settings.UpdateProfileRequest
import org.lsm.flower_mailing.data.settings.UserProfile
import org.lsm.flower_mailing.notifications.TopicManager
import org.lsm.flower_mailing.remote.RetrofitClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val api = RetrofitClient.getInstance(application)
    private val repository = UserPreferencesRepository(application)
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)
    var editFirstName by mutableStateOf("")
    var editLastName by mutableStateOf("")
    var editJabatan by mutableStateOf("")
    var editAtribut by mutableStateOf("")
    var oldPassword by mutableStateOf("")
    var newPassword by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = api.getMyProfile()
                if (response.status == "success") {
                    _userProfile.value = response.data
                    response.data?.let {
                        editFirstName = it.firstName ?: ""
                        editLastName = it.lastName ?: ""
                        editJabatan = it.jabatan ?: ""
                        editAtribut = it.atribut ?: ""
                    }
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun updateProfile(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                val request = UpdateProfileRequest(
                    firstName = editFirstName,
                    lastName = editLastName,
                    jabatan = editJabatan,
                    atribut = editAtribut
                )
                val response = api.updateMyProfile(request)
                if (response.status == "success") {
                    _userProfile.value = response.data
                    successMessage = "Profil berhasil diperbarui"
                    onSuccess()
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
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
            try {
                val request = ChangePasswordRequest(
                    oldPassword = oldPassword,
                    newPassword = newPassword,
                    confirmPassword = confirmPassword
                )
                val response = api.changePassword(request)
                if (response.status == "success") {
                    successMessage = "Password berhasil diubah"
                    oldPassword = ""
                    newPassword = ""
                    confirmPassword = ""
                    onSuccess()
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                val token = repository.refreshTokenFlow.first()
                val role = repository.userRoleFlow.first()

                if (role != null) {
                    TopicManager.unsubscribeFromRole(role)
                }

                if (token != null) {
                    api.logout(LogoutRequest(token))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                withContext(NonCancellable) {
                    repository.clearLoginData()
                }
                onLogoutComplete()
            }
        }
    }
}