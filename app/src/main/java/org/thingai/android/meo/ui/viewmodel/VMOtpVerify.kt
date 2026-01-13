package org.thingai.android.meo.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VMOtpVerify @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    data class OtpVerificationUiState(
        val phone: String = "",
        val otp: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val resendEnabled: Boolean = true,
        val resendCountdownSeconds: Int = 0,
    ) {
        val canVerify: Boolean get() = otp.length == 4 && !isLoading
    }
    companion object {
        const val ARG_PHONE = "phone"
        private const val OTP_LENGTH = 4
        private const val RESEND_COOLDOWN = 30
    }

    private val _uiState = MutableStateFlow(
        OtpVerificationUiState(
            phone = savedStateHandle.get<String>(ARG_PHONE).orEmpty()
        )
    )
    val uiState: StateFlow<OtpVerificationUiState> = _uiState

    private var countdownJob: Job? = null

    fun onOtpChanged(value: String) {
        val digitsOnly = value.filter { it.isDigit() }.take(OTP_LENGTH)
        _uiState.update { it.copy(otp = digitsOnly, errorMessage = null) }
    }

    fun verifyOtp(onVerified: (phone: String) -> Unit = {}) {
        val state = _uiState.value
        if (!state.canVerify) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // TODO: Integrate with your service: authRepository.verifyOtp(state.phone, state.otp)
                delay(800)
                _uiState.update { it.copy(isLoading = false) }
                onVerified(state.phone)
            } catch (t: Throwable) {
                _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: "Xác nhận thất bại") }
            }
        }
    }

    fun resendCode() {
        val state = _uiState.value
        if (!state.resendEnabled) return

        viewModelScope.launch {
            try {
                // TODO: authRepository.sendOtp(state.phone)
                _uiState.update { it.copy(resendEnabled = false, resendCountdownSeconds = RESEND_COOLDOWN) }
                startCountdown()
            } catch (t: Throwable) {
                _uiState.update { it.copy(errorMessage = t.message ?: "Gửi lại mã thất bại") }
            }
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var remaining = RESEND_COOLDOWN
            while (remaining > 0) {
                delay(1000)
                remaining -= 1
                _uiState.update { it.copy(resendCountdownSeconds = remaining) }
            }
            _uiState.update { it.copy(resendEnabled = true, resendCountdownSeconds = 0) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}