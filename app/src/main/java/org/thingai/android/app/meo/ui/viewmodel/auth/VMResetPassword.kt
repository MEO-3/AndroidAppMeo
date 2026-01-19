package org.thingai.android.app.meo.ui.viewmodel.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VMResetPassword @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        const val ARG_PHONE = "phone" // route arg kept for compatibility (contains email)
        const val ARG_OTP = "otp"
    }

    data class ResetPasswordUiState(
        val email: String = "",
        val otp: String = "",
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
            email = savedStateHandle.get<String>(ARG_PHONE).orEmpty(),
            otp = savedStateHandle.get<String>(ARG_OTP).orEmpty()
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
                val res = org.thingai.android.module.meo.MeoSdk.authHandler().resetPassword(state.email, state.otp, state.newPassword)
                if (res.isSuccess) {
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                } else {
                    val ex = res.exceptionOrNull()
                    _uiState.update { it.copy(isLoading = false, errorMessage = ex?.message ?: "Cập nhật mật khẩu thất bại") }
                }
            } catch (t: Throwable) {
                _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: "Cập nhật mật khẩu thất bại") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}