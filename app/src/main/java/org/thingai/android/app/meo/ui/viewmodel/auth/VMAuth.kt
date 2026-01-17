package org.thingai.android.app.meo.ui.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import org.thingai.android.app.meo.data.remote.auth.AuthRepository

@HiltViewModel
class VMAuth @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {
    data class AuthUiState(
        val phoneNumber: String = "",
        val password: String = "",
        val isPasswordVisible: Boolean = false,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,

        // explicit signup field
        val confirmPassword: String = "",
        val username: String = "",
        val email: String = ""
    )

    sealed class AuthEvent {
        object LoginSuccess : AuthEvent()
        object SignupSuccess : AuthEvent()
        data class ShowError(val message: String) : AuthEvent()
    }

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

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

    fun onEmailChanged(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null)
    }

    fun onUsernameChanged(value: String) {
        _uiState.value = _uiState.value.copy(username = value, errorMessage = null)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    fun login() {
        val current = _uiState.value
        val authUsername = current.email

        if (authUsername.isBlank() || current.password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Username and password are required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val res = repo.login(authUsername, current.password)
            if (res.isSuccess) {
                _events.send(AuthEvent.LoginSuccess)
            } else {
                val msg = res.exceptionOrNull()?.message ?: "Login failed"
                _uiState.value = _uiState.value.copy(errorMessage = msg)
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun signup() {
        val current = _uiState.value
        if (current.username.isBlank() || current.email.isBlank() || current.phoneNumber.isBlank() || current.password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "All fields are required")
            return
        }
        if (current.password != current.confirmPassword) {
            _uiState.value = _uiState.value.copy(errorMessage = "Passwords do not match")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val res = repo.signup(
                username = current.username,
                email = current.email,
                phoneNumber = current.phoneNumber,
                authUsername = current.email, // use email as auth username
                password = current.password
            )
            if (res.isSuccess) {
                _events.send(AuthEvent.SignupSuccess)
            } else {
                // switch error message to match backend response
                val msg = res.exceptionOrNull()?.message ?: "Signup failed"
                _uiState.value = _uiState.value.copy(errorMessage = msg)
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}