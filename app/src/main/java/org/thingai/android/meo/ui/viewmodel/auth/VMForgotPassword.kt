package org.thingai.android.meo.ui.viewmodel.auth

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
class VMForgotPassword @Inject constructor() : ViewModel() {
    data class ForgotPasswordUiState(
        val phone: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isCodeSent: Boolean = false,
    ) {
        val canSubmit: Boolean get() = phone.isNotBlank() && !isLoading
    }

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState

    fun onPhoneChanged(newPhone: String) {
        _uiState.update { it.copy(phone = newPhone, errorMessage = null) }
    }

    fun sendCode() {
        val current = _uiState.value
        if (!current.canSubmit) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // TODO: Integrate with your service, e.g. authRepository.sendOtp(phone)
                delay(800) // simulate network
                _uiState.update { it.copy(isLoading = false, isCodeSent = true) }
            } catch (t: Throwable) {
                _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: "Gửi mã thất bại") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}