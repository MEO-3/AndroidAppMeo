package org.thingai.android.app.meo.ui.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thingai.android.module.meo.MeoSdk
import javax.inject.Inject

@HiltViewModel
class VMForgotPassword @Inject constructor() : ViewModel() {
    data class ForgotPasswordUiState(
        val email: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isCodeSent: Boolean = false,
    ) {
        val canSubmit: Boolean get() = email.isNotBlank() && !isLoading
    }

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState

    fun onEmailChanged(newEmail: String) {
        _uiState.update { it.copy(email = newEmail, errorMessage = null) }
    }

    fun sendCode() {
        val current = _uiState.value
        if (!current.canSubmit) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Use MeoSdk auth handler to request password reset OTP
                val result = MeoSdk.authHandler().requestPasswordReset(current.email, null)
                if (result.isSuccess) {
                    _uiState.update { it.copy(isLoading = false, isCodeSent = true) }
                } else {
                    val ex = result.exceptionOrNull()
                    _uiState.update { it.copy(isLoading = false, errorMessage = ex?.message ?: "Gửi mã thất bại") }
                }
            } catch (t: Throwable) {
                _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: "Gửi mã thất bại") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearCodeSent() {
        _uiState.update { it.copy(isCodeSent = false) }
    }
}