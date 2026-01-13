package org.thingai.android.meo.ui.viewmodel.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VMResetPassword @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    // Inject your repo/service, e.g. private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        const val ARG_PHONE = "phone"
    }

    data class ResetPasswordUiState(
        val phone: String = "",
        val newPassword: String = "",
        val confirmPassword: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isSaved: Boolean = false
    ) {
        val isValid: Boolean
            get() = newPassword.length >= 6 &&
                confirmPassword.length >= 6 &&
                newPassword == confirmPassword &&
                !isLoading
    }

    private val _uiState = MutableStateFlow(
        ResetPasswordUiState(
            phone = savedStateHandle.get<String>(ARG_PHONE).orEmpty()
        )
    )
    val uiState: StateFlow<ResetPasswordUiState> = _uiState

    fun onNewPasswordChanged(value: String) {
        _uiState.update { it.copy(newPassword = value, errorMessage = null) }
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    }

    fun saveNewPassword() {
        val state = _uiState.value
        if (!state.isValid) {
            _uiState.update { it.copy(errorMessage = "Mật khẩu không hợp lệ hoặc không khớp") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // TODO: Replace with real call, e.g. authRepository.updatePassword(state.phone, state.newPassword)
                delay(800)
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = t.message ?: "Cập nhật mật khẩu thất bại"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}