package org.thingai.android.meo.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class VMAuth: ViewModel() {
    data class AuthUiState(
        val phoneNumber: String = "",
        val password: String = "",
        val isPasswordVisible: Boolean = false,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,

        // explicit signup field
        val confirmPassword: String = "",
    )

    sealed class AuthEvent {
            object LoginSuccess: AuthEvent()
            object SignupSuccess: AuthEvent()
            data class ShowError(val message: String): AuthEvent()
        }

        private val _uiState = MutableStateFlow(AuthUiState())
        val uiState: StateFlow<VMAuth.AuthUiState> = _uiState.asStateFlow()

        private val _events = Channel<AuthEvent>(Channel.BUFFERED)
        val events = _events.receiveAsFlow()

        fun onPhoneNumberChanged(value: String) {
            _uiState.value = _uiState.value.copy(phoneNumber = value, errorMessage = null)
        }

        fun onPasswordChanged(value: String) {
            _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
        }

        fun onConfirmPasswordChanged(value: String) {
            _uiState.value = _uiState.value.copy(confirmPassword = value, errorMessage = null)
        }

        fun togglePasswordVisibility() {
            _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
        }

        fun login() {
    //        val current = _uiState.value
    //
    //        if (!validateEmail(current.phoneNumber)) return
    //        if (!validatePassword(current.password)) return
    //
    //        if (current.phoneNumber != "avis@cts.com" || current.password != "Avis@2026") {
    //            emitEvent(AuthEvent.ShowError("Email hoặc mật khẩu không đúng"))
    //            return
    //        }

            emitEvent(AuthEvent.LoginSuccess)
        }

        fun signup() {

        }

        private fun emitEvent(event: AuthEvent) {
            viewModelScope.launch {
                _events.send(event)
            }
        }

        private fun validateEmail(email: String): Boolean {
            if (email.isEmpty()) {
                _uiState.value = _uiState.value.copy(errorMessage = "Email is required")
                return false;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _uiState.value = _uiState.value.copy(errorMessage = "Email is not valid")
                return false;
            }

            return true;
        }

        private fun validatePassword(password: String): Boolean {
            if (password.isEmpty()) {
                _uiState.value = _uiState.value.copy(errorMessage = "Password is required")
                return false
            }

            if (password.length < 6) {
                _uiState.value = _uiState.value.copy(errorMessage = "Password must be at least 6 characters")
                return false
            }

            return true
        }
}