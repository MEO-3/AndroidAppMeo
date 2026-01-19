package org.thingai.android.app.meo.ui.screen.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import org.thingai.android.app.meo.navigation.Route
import org.thingai.android.app.meo.ui.shared.dialog.ErrorDialog
import org.thingai.android.app.meo.ui.viewmodel.auth.VMAuth

@Composable
fun SignUpScreen(
    navController: NavHostController,
    vm: VMAuth = hiltViewModel()
) {
    val ui = vm.uiState.collectAsStateWithLifecycle().value

    var dialogMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                is VMAuth.AuthEvent.SignupSuccess -> {
                    // after successful signup, navigate to device list
                    navController.navigate(Route.DEVICE_LIST) {
                        launchSingleTop = true
                        popUpTo(navController.graph.startDestinationId) { saveState = false }
                    }
                }
                is VMAuth.AuthEvent.ShowError -> {
                    dialogMessage = event.message
                }
                else -> Unit
            }
        }
    }

    ErrorDialog(
        show = dialogMessage != null,
        message = dialogMessage,
        onDismiss = {
            dialogMessage = null
        }
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Create new account",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Empowering DIY and STEAM with MEO.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(Modifier.height(24.dp))

            // Username
            OutlinedTextField(
                value = ui.username,
                onValueChange = vm::onUsernameChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Username",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                placeholder = { Text("Username") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(Modifier.height(12.dp))

            // Email
            OutlinedTextField(
                value = ui.email,
                onValueChange = vm::onEmailChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                placeholder = { Text("Email address") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(Modifier.height(12.dp))

            // Phone
            OutlinedTextField(
                value = ui.phoneNumber,
                onValueChange = vm::onPhoneNumberChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone number",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                placeholder = { Text("Enter phone number") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(Modifier.height(12.dp))

            // Password
            OutlinedTextField(
                value = ui.password,
                onValueChange = vm::onPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { vm.togglePasswordVisibility() }) {
                        Icon(
                            imageVector = if (ui.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Password visibility"
                        )
                    }
                },
                placeholder = { Text("Enter password") },
                visualTransformation = if (ui.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(Modifier.height(12.dp))

            // Confirm Password
            OutlinedTextField(
                value = ui.confirmPassword,
                onValueChange = vm::onConfirmPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Confirm password",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { vm.togglePasswordVisibility() }) {
                        Icon(
                            imageVector = if (ui.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Password visibility"
                        )
                    }
                },
                placeholder = { Text("Re-enter password") },
                visualTransformation = if (ui.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(Modifier.height(12.dp))

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { vm.signup() },
                enabled = !ui.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.filledTonalButtonColors()
            ) {
                if (ui.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Text("Sign up",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Sign in",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        navController.navigate(Route.LOGIN) {
                            launchSingleTop = true
                            popUpTo(navController.graph.startDestinationId) { saveState = false }
                        }
                    }
                )
            }
        }
    }
}