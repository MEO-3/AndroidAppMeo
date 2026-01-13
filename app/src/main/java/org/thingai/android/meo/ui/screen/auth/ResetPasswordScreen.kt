package org.thingai.android.meo.ui.screen.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import org.thingai.android.meo.ui.shared.appbar.BaseTopAppBar
import org.thingai.android.meo.ui.viewmodel.auth.VMResetPassword

@Composable
fun ResetPasswordScreen(
    navController: NavHostController,
    viewModel: VMResetPassword = hiltViewModel()
) {
    val ui = viewModel.uiState.collectAsState().value

    // Navigate up when saved, consistent with other auth screens
    if (ui.isSaved) {
        navController.navigateUp()
        return
    }

    var pwVisible by rememberSaveable { mutableStateOf(false) }
    var pwConfirmVisible by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar consistent with Forgot/OTP screens
            BaseTopAppBar(
                onBack = {navController.popBackStack()},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 24.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Reset password",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(24.dp))

                // New password
                OutlinedTextField(
                    value = ui.newPassword,
                    onValueChange = viewModel::onNewPasswordChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter new password") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "New password",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { pwVisible = !pwVisible }) {
                            Icon(
                                imageVector = if (pwVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Password visibility"
                            )
                        }
                    },
                    visualTransformation = if (pwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(Modifier.height(12.dp))

                // Confirm password
                OutlinedTextField(
                    value = ui.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Re-enter password") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Verify password",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { pwConfirmVisible = !pwConfirmVisible }) {
                            Icon(
                                imageVector = if (pwConfirmVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Password Visibility"
                            )
                        }
                    },
                    visualTransformation = if (pwConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (ui.isValid) viewModel.saveNewPassword()
                        }
                    )
                )

                if (ui.errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = ui.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.saveNewPassword() },
                    enabled = ui.isValid && !ui.isLoading,
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
                        Text("Save",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}